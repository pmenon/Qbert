package net.qbert.channel

import net.qbert.connection.AMQConnection

import net.qbert.framing.{ ContentBody, ContentHeader, Frame, Method }
import net.qbert.logging.Logging
import net.qbert.message.{ MessagePublishInfo, PartialMessage }
import net.qbert.protocol.AMQProtocolSession
import net.qbert.state.{ State, StateDriven }

import scala.actors.Actor
import scala.actors.Actor._

object AMQChannel {
  val systemChannelId = 0
}

sealed abstract case class ChannelMessage()
case class PublishReceived(info: MessagePublishInfo) extends ChannelMessage()
case class FrameReceived(f: Frame) extends ChannelMessage
case class Close() extends ChannelMessage

class AMQChannel(val channelId: Int, val session: AMQProtocolSession) extends Actor with Logging {
  info("Channel created with id {} ", channelId)
  
  val initialState = State.opened
  private var partialMessage: Option[PartialMessage] = None
  
  // start as soon as we instantiate
  start

  def publishReceived(info: MessagePublishInfo) = this ! PublishReceived(info)
  def frameReceived(f: Frame) = this ! FrameReceived(f)
  def close() = this ! Close()

  def act() = loop(mainLoop)

  def mainLoop() = react {
    case PublishReceived(info) => handlePublishFrame(info)
    case Close() => closeChannel()
  }

  def handlePublishFrame(info: MessagePublishInfo) = {
    //info("Channel {} received basic.publish", channelId)
    partialMessage match {
      case Some(existing) => println("waiting for")
    }
    //partialMessage match {
    //  case Some(m) => info("")
    //}
  }

  def handleContentHeader(header: ContentHeader) = {} 
  def handleContentHeader(contenBody: ContentBody) = {}

  def closeChannel() = {
    //do much much more here
    println("closing channel")
    exit()
  }

}
