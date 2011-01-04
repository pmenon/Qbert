package net.qbert.channel

import net.qbert.connection.AMQConnection
import net.qbert.framing.{ ContentBody, ContentHeader }
import net.qbert.util.{ ActorDelegate, Logging }
import net.qbert.message.{ AMQMessage, MessagePublishInfo, PartialMessage }
import net.qbert.protocol.AMQProtocolSession
import net.qbert.queue.{ AMQQueue, QueueEntry, QueueConsumer }
import net.qbert.subscription.Subscription

import akka.actor.Actor
import scala.collection.mutable

object AMQChannel {
  val systemChannelId = 0
}

// The messages an AMQChannel handles
sealed abstract class ChannelMessage()
case class PublishMessage(info: MessagePublishInfo, header: ContentHeader, body: ContentBody) extends ChannelMessage
case class PublishReceived(info: MessagePublishInfo) extends ChannelMessage()
case class ContentHeaderReceived(header: ContentHeader) extends ChannelMessage()
case class ContentBodyReceived(body: ContentBody) extends ChannelMessage()
case class DeliverMessage(m: AMQMessage) extends ChannelMessage
case class SubscriptionRequest[T](consumerTag: String, q: AMQQueue, sFun: (String) => T, eFun: (String) => T) extends ChannelMessage
case object StopChannel extends ChannelMessage


class AMQChannel(val channelId: Int, val conn: AMQConnection) extends ActorDelegate with QueueConsumer with Logging {
  log.info("Channel created with id {} ", channelId)

  private var deliveryTag: Long = 0
  private var consumerTag: Long = 0
  private val subscriptions = mutable.Map[String, Subscription]()
  private var partialMessage: Option[PartialMessage] = None
  private val channelActor = Actor.actorOf(new AMQChannelActor).start

  // the main API for a channel
  def publishReceived(info: MessagePublishInfo) = invokeNoResult(channelActor, PublishReceived(info))
  def publishContentHeader(header: ContentHeader) = invokeNoResult(channelActor, ContentHeaderReceived(header))
  def publishContentBody(body: ContentBody) = invokeNoResult(channelActor, ContentBodyReceived(body))
  def deliverMessage(s: Subscription, m: AMQMessage) = invokeNoResult(channelActor, DeliverMessage(m))
  def subscribeToQueue[T](consumerTag: String, q: AMQQueue)(sFun: (String) => T, eFun: (String) => T) = invokeNoResult(channelActor, SubscriptionRequest(consumerTag, q, sFun, eFun))
  def stop() = {
    invokeNoResult(channelActor, StopChannel)
    channelActor.stop
  }

  private class AMQChannelActor extends Actor {

    def receive = {
      case PublishReceived(info) => handlePublishFrame(info)
      case ContentHeaderReceived(header) => handleContentHeader(header)
      case ContentBodyReceived(body) => handleContentBody(body)
      case DeliverMessage(m) => handleMessageDelivery(m)
      case SubscriptionRequest(tag, q, sFun, eFun) => handleSubscriptionRequest(tag, q, sFun, eFun)
      case StopChannel => stopChannel()
      case _ => log.error("Received unknown message on channel {} ...", channelId)
    }

  }

  private def nextDeliveryTag = {
    deliveryTag += 1
    deliveryTag
  }

  private def nextConsumerTag = {
    consumerTag += 1
    consumerTag
  }

  private def handleSubscriptionRequest[T](consumerTag: String, q: AMQQueue, sFun: (String) => T, eFun: (String) => T) = {
    subscriptions.get(consumerTag) match {
      case Some(subscription) =>
        log.info("Subscription {} already exists for channel {}, closing connection ...", consumerTag, channelId)
        eFun("ERROR")
      case None =>
        val tag = if(consumerTag.isEmpty) "qbert_gen_" + nextConsumerTag else consumerTag
        log.info("Creating subscription with tag {} on channel {} ...", tag, channelId)
        val subscription = Subscription(tag, this, q)
        q.addSubscription(subscription)
        subscriptions(tag) = subscription
        sFun(tag)
    }
  }

  private def handleMessageDelivery(m: AMQMessage) = {
    val messageDeliveryTag = deliveryTag + 1
    //val deliver = session.methodFactory.createBasicDeliver(m.logInfo.)
  }

  private def handleUndeliveredMessage(m: AMQMessage) = {
    println("Message had no consumers but was immediate!")
  }
  
  private def handleUnroutableMessage(m: AMQMessage) {
    println("Message was unroutable but was mandatory!")
  }

  private def handlePublishFrame(publishInfo: MessagePublishInfo) = {
    log.info("Channel {} received basic.publish {}", channelId, publishInfo)
    partialMessage match {
      case None => partialMessage = Some(PartialMessage(Some(publishInfo), None))
      case _ => error("Received basic.publish while waiting for content")
    }
  }

  private def handleContentHeader(header: ContentHeader) = {
    log.info("Channel {} received content header {}", channelId, header)
    partialMessage match {
      case Some(PartialMessage(Some(p), None)) => 
        partialMessage = Some(PartialMessage(Some(p), Some(header)))
      case _ => error("Received header without publish frame")
    }
  }

  private def handleContentBody(body: ContentBody) = {
    log.info("Channel {} received content body {}", channelId, new String(body.buffer, "utf-8"))
    partialMessage match {
      case Some(PartialMessage(Some(p), Some(h))) => 
        partialMessage.get.addContent(body) 
        if(partialMessage.get.sizeLeft == 0) deliverMessage
      case _ => error("Unexpected frame received.  Either header was not received before body or publish was not received")
    }
  }

  private def deliverMessage() = {
    val message = new AMQMessage(1, partialMessage.get.info.get, partialMessage.get.header.get, partialMessage.get.body)
    if(message.isPersistent()) conn.virtualHost.store.storeMessage(message)

    val exchange = conn.virtualHost.lookupExchange(message.info.exchangeName)
    val queues = exchange.map(_.route(message, message.info.routingKey)).getOrElse(List[AMQQueue]())

    if(message.isMandatory && queues.length <= 0) {
      // a mandatory message must be routable
      handleUnroutableMessage(message)
    } else if (message.isImmediate()) {
      // an immediate message must be delivered to atleast a single consumer on a single queue
      val res = queues.map(_.enqueueAndWait(message)).foldLeft(false)( (acc,res) => acc || res.await.result.map(_.delivered).getOrElse(false) )
      if(!res) handleUndeliveredMessage(message)
    } else {
      queues.foreach(_.enqueue(message))
    }

    partialMessage = None
  }

  def stopChannel() = {
    //do much much more here
    log.info("Channel {} is stopping ... ", channelId)
  }


  def onEnqueue(consumerTag: String, entry: QueueEntry) = {
    val deliver = conn.methodFactory.createBasicDeliver(consumerTag, nextDeliveryTag, false, entry.msg.m.info.exchangeName, entry.msg.m.info.routingKey)
    conn.writeFrame(deliver.generateFrame(channelId))
    conn.writeFrame(entry.msg.m.header.generateFrame(channelId))
    conn.writeFrame(entry.msg.m.body.generateFrame(channelId))
    true
  }

  def onDequeue(entry: QueueEntry) = { true }


}
