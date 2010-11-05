package net.qbert.channel

import net.qbert.connection.AMQConnection

import net.qbert.framing.{ ContentBody, ContentHeader, Frame, Method }
import net.qbert.logging.Logging
import net.qbert.message.{ AMQMessage, MessagePublishInfo, PartialMessage }
import net.qbert.protocol.AMQProtocolSession
import net.qbert.subscription.Subscription

import scala.actors.Actor
import scala.actors.Actor._
import net.qbert.queue.{AMQQueue, QueueEntry, QueueConsumer}

object AMQChannel {
  val systemChannelId = 0
}

sealed abstract class ChannelMessage()
case class PublishReceived(info: MessagePublishInfo) extends ChannelMessage()
case class ContentHeaderReceived(header: ContentHeader) extends ChannelMessage()
case class ContentBodyReceived(body: ContentBody) extends ChannelMessage()
case class FrameReceived(f: Frame) extends ChannelMessage
case class Close() extends ChannelMessage

class AMQChannel(val channelId: Int, val session: AMQProtocolSession) extends Actor with QueueConsumer with Logging {
  info("Channel created with id {} ", channelId)
  
  private var partialMessage: Option[PartialMessage] = None
  
  // start as soon as we instantiate
  start

  def publishReceived(info: MessagePublishInfo) = this ! PublishReceived(info)
  def publishContentHeader(header: ContentHeader) = this ! ContentHeaderReceived(header)
  def publishContentBody(body: ContentBody) = this ! ContentBodyReceived(body)
  def frameReceived(f: Frame) = this ! FrameReceived(f)
  def close() = this ! Close()

  def act() = loop(mainLoop)

  def mainLoop() = react {
    case PublishReceived(info) => handlePublishFrame(info)
    case ContentHeaderReceived(header) => handleContentHeader(header)
    case ContentBodyReceived(body) => handleContentBody(body)
    case Close() => closeChannel()
  }

  def handlePublishFrame(publishInfo: MessagePublishInfo) = {
    info("Channel {} received basic.publish {}", channelId, publishInfo)
    partialMessage match {
      case None => partialMessage = Some(PartialMessage(Some(publishInfo), None))
      case _ => error("Received basic.publish while waiting for content")
    }
  }

  def handleContentHeader(header: ContentHeader) = {
    info("Channel {} received content header {}", channelId, header)
    partialMessage match {
      case Some(PartialMessage(Some(p), None)) => 
        partialMessage = Some(PartialMessage(Some(p), Some(header)))
      case _ => error("Received header without publish frame")
    }
  }

  def handleContentBody(body: ContentBody) = {
    info("Channel {} received content body {}", channelId, body)
    partialMessage match {
      case Some(PartialMessage(Some(p), Some(h))) => 
        partialMessage.get.addContent(body) 
        if(partialMessage.get.sizeLeft == 0) deliverMessage
      case _ => error("Unexpected frame received.  Either header was not received before body or publish was not received")
    }
  }

  def deliverMessage() = {
    val message = AMQMessage(1, partialMessage.get.info.get, partialMessage.get.header.get, partialMessage.get.body)
    val exchange = session.virtualHost.map(_.lookupExchange(message.info.exchangeName)).getOrElse(None)
    val queues = exchange.map(_.route(message, message.info.routingKey)).getOrElse(List[AMQQueue]())
    queues.foreach(_.enqueue(message))
  }

  def closeChannel() = {
    //do much much more here
    println("closing channel")
    exit()
  }


  def onEnqueue(e: QueueEntry) = {}
  def onDequeue(e: QueueEntry) = {}


}
