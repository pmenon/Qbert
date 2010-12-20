package net.qbert.channel

import net.qbert.error.QbertError
import net.qbert.framing.{ ContentBody, ContentHeader, Frame }
import net.qbert.logging.Logging
import net.qbert.message.{ AMQMessage, MessagePublishInfo, PartialMessage }
import net.qbert.protocol.AMQProtocolSession
import net.qbert.queue.{ AMQQueue, QueueEntry, QueueConsumer }
import net.qbert.subscription.Subscription

import scala.actors.Actor
import scala.collection.mutable

object AMQChannel {
  val systemChannelId = 0
}

sealed abstract class ChannelErrors extends QbertError
case class ChannelDoesNotExist(i: Int) extends ChannelErrors

// The messages an AMQChannel handles
sealed abstract class ChannelMessage()
case class PublishReceived(info: MessagePublishInfo) extends ChannelMessage()
case class ContentHeaderReceived(header: ContentHeader) extends ChannelMessage()
case class ContentBodyReceived(body: ContentBody) extends ChannelMessage()
case class DeliverMessage(m: AMQMessage) extends ChannelMessage
case class SubscriptionRequest(consumerTag: String, q: AMQQueue) extends ChannelMessage
case object StopChannel extends ChannelMessage

class AMQChannel(val channelId: Int, val session: AMQProtocolSession) extends Actor with QueueConsumer with Logging {
  info("Channel created with id {} ", channelId)

  private var deliveryTag: Long = 0
  private val subscriptions = mutable.Map[String, Subscription]()
  private var partialMessage: Option[PartialMessage] = None
  
  // start as soon as we instantiate
  start

  // the main API for a channel
  def publishReceived(info: MessagePublishInfo) = this ! PublishReceived(info)
  def publishContentHeader(header: ContentHeader) = this ! ContentHeaderReceived(header)
  def publishContentBody(body: ContentBody) = this ! ContentBodyReceived(body)
  def deliverMessage(s: Subscription, m: AMQMessage) = this ! DeliverMessage(m)
  def subscribeToQueue(consumerTag: String, q: AMQQueue) = this ! SubscriptionRequest(consumerTag, q)
  def stop() = this ! StopChannel

  def act() = loop(mainLoop)

  def mainLoop() = react {
    case PublishReceived(info) => handlePublishFrame(info)
    case ContentHeaderReceived(header) => handleContentHeader(header)
    case ContentBodyReceived(body) => handleContentBody(body)
    case DeliverMessage(m) => handleMessageDelivery(m)
    case SubscriptionRequest(tag, q) => handleSubscriptionRequest(tag, q)
    case StopChannel => stopChannel()
  }

  private def handleSubscriptionRequest(consumerTag: String, q: AMQQueue) = {
    subscriptions.get(consumerTag) match {
      case Some(subscription) => exit()
      case None =>
        val subscription = Subscription(consumerTag, this, q)
        q.addSubscription(subscription)
        subscriptions(consumerTag) = subscription
    }
  }

  private def handleMessageDelivery(m: AMQMessage) = {
    val messageDeliveryTag = deliveryTag + 1
    //val deliver = session.methodFactory.createBasicDeliver(m.info.)
  }

  private def handleUndeliveredMessage(m: AMQMessage) = {
    println("Message had no consumers but was immediate!")
  }
  
  private def handleUnroutableMessage(m: AMQMessage) {
    println("Message was unroutable but was mandatory!")
  }

  private def handlePublishFrame(publishInfo: MessagePublishInfo) = {
    info("Channel {} received basic.publish {}", channelId, publishInfo)
    partialMessage match {
      case None => partialMessage = Some(PartialMessage(Some(publishInfo), None))
      case _ => error("Received basic.publish while waiting for content")
    }
  }

  private def handleContentHeader(header: ContentHeader) = {
    info("Channel {} received content header {}", channelId, header)
    partialMessage match {
      case Some(PartialMessage(Some(p), None)) => 
        partialMessage = Some(PartialMessage(Some(p), Some(header)))
      case _ => error("Received header without publish frame")
    }
  }

  private def handleContentBody(body: ContentBody) = {
    info("Channel {} received content body {}", channelId, new String(body.buffer, "utf-8"))
    partialMessage match {
      case Some(PartialMessage(Some(p), Some(h))) => 
        partialMessage.get.addContent(body) 
        if(partialMessage.get.sizeLeft == 0) deliverMessage
      case _ => error("Unexpected frame received.  Either header was not received before body or publish was not received")
    }
  }

  private def deliverMessage() = {
    val message = new AMQMessage(1, partialMessage.get.info.get, partialMessage.get.header.get, partialMessage.get.body)
    if(message.isPersistent()) session.virtualHost.foreach(_.store.storeMessage(message))

    val exchange = session.virtualHost.map(_.lookupExchange(message.info.exchangeName)).getOrElse(None)
    val queues = exchange.map(_.route(message, message.info.routingKey)).getOrElse(List[AMQQueue]())

    if(message.isMandatory && queues.length <= 0) {
      // a mandatory message must be routable
      handleUnroutableMessage(message)
    } else if (message.isImmediate()) {
      // an immediate message must be delivered to atleast a single consumer on a single queue
      val res = queues.map(_.enqueueAndWait(message)).foldLeft(false)( (acc,res) => acc || res().delivered )
      if(!res) handleUndeliveredMessage(message)
    } else {
      queues.foreach(_.enqueue(message))
    }

    partialMessage = None
  }

  def stopChannel() = {
    //do much much more here
    info("Channel {} is stopping ... ", channelId)
    exit()
  }


  def onEnqueue(entry: QueueEntry) = { true }
  def onDequeue(entry: QueueEntry) = { true }


}
