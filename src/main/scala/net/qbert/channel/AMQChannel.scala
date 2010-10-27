package net.qbert.channel

import net.qbert.connection.AMQConnection

import net.qbert.framing.{ ContentBody, ContentHeader, Frame, Method }
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolSession

import net.qbert.protocol.AMQProtocolDriver

import scala.actors.Actor
import scala.actors.Actor._

object AMQChannel {
  val systemChannelId = 0
}

sealed abstract case class ChannelMessage
case class FrameReceived(f: Frame) extends ChannelMessage
case class Close() extends ChannelMessage

class AMQChannel(val channelId: Int, val session: AMQProtocolSession) extends Actor with Logging {
  info("Channel created with id = " + channelId)
  
  // start as soon as we instantiate
  start

  def frameReceived(f: Frame) = this ! FrameReceived(f)
  def close() = this ! Close()

  def act() = loop(mainLoop)

  def mainLoop() = react {
    case FrameReceived(frame) => handleFrame(frame)
    case Close() => closeChannel()
  }

  def handleFrame(f: Frame): Unit = f.typeId match {
    case Frame.FRAME_METHOD => methodReceived(f.payload.asInstanceOf[Method])
    case Frame.FRAME_CONTENT => contentHeaderReceived(f.payload.asInstanceOf[ContentHeader])
    case Frame.FRAME_BODY => contentBodyReceived(f.payload.asInstanceOf[ContentBody])
    case Frame.FRAME_HEARTBEAT => heartbeatReceived(f)
    case _ => error("uknown frame type received")
  }

  def methodReceived(method: Method) = {
    info("Method received ... " + method)
    session.asInstanceOf[AMQProtocolDriver].methodHandler.handleMethod(channelId, method)
  }

  def contentHeaderReceived(header: ContentHeader) = {} 
  def contentBodyReceived(contenBody: ContentBody) = {}
  def heartbeatReceived(f: Frame) = {}

  def closeChannel() = {
    //do much much more here
    println("closing channel")
    exit()
  }

}
