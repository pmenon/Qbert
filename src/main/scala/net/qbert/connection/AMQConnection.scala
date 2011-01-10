package net.qbert.connection

import net.qbert.channel.ChannelManager
import net.qbert.protocol.ProtocolVersion
import net.qbert.framing.{ AMQDataBlock, ContentBody, ContentHeader, Frame, Method, MethodFactory }
import net.qbert.handler.{ MethodErrorResponse, MethodSuccessResponse, NoResponseMethodSuccess }
import net.qbert.network.{ Connection, FrameDecoder, FrameHandler }
import net.qbert.util.Logging
import net.qbert.virtualhost.AMQVirtualHost
import net.qbert.handler.MethodHandler

class ConnectionProperties(val protocolVersion: ProtocolVersion, val maxChannel: Int, val maxFrameSize: Int, val hearbeat: Int)

class AMQConnection(val conn: Connection, val decoder: FrameDecoder, val connProperties: ConnectionProperties, val virtualHost: AMQVirtualHost) extends FrameHandler with ChannelManager with Logging {
  //val channelManager = new ChannelManager { val maxChannels = connProperties.maxChannel }
  val maxChannels = connProperties.maxChannel
  val methodFactory = MethodFactory.createWithVersion(connProperties.protocolVersion)
  var methodHandler: MethodHandler = MethodHandler(this)

  def writeFrame(dataBlock: AMQDataBlock) = conn.writeFrame(dataBlock)

  def handleFrame(dataBlock: AMQDataBlock) = {
    val frame = dataBlock.asInstanceOf[Frame]
    frame.typeId match {
      case Frame.FRAME_METHOD => methodReceived(frame.channelId, frame.payload.asInstanceOf[Method])
      case Frame.FRAME_CONTENT => contentHeaderReceived(frame.channelId, frame.payload.asInstanceOf[ContentHeader])
      case Frame.FRAME_BODY => contentBodyReceived(frame.channelId, frame.payload.asInstanceOf[ContentBody])
      case _ => handleUnknownFrame(frame)
    }
  }

  def handleUnknownFrame(frame: Frame) = {
    log.error("Unknown frame received {}, closing the connection ...", frame)
    conn.close
  }

  def handleError() = {}

  def handleResponse(frame: Frame) = {
    conn.writeFrame(frame)
  }

  def methodReceived(channelId: Int, method: Method) = {
    log.info("Method received : {}", method)
    val result = methodHandler.handleMethod(channelId, method)
    result match {
      case MethodErrorResponse(error) => handleError()
      case MethodSuccessResponse(response) => handleResponse(response)
      case NoResponseMethodSuccess =>
    }
  }

  def contentHeaderReceived(channelId: Int, header: ContentHeader) = {
    log.info("Content header received : {}", header)
    getChannel(channelId).map( (channel) =>
      channel.publishContentHeader(header)
    ).orElse(error("Channel " + channelId + " does not exist"))
  }

  def contentBodyReceived(channelId: Int, body: ContentBody) = {
    log.info("Content body received : {} ", body)
    getChannel(channelId).map( (channel) =>
      channel.publishContentBody(body)
    ).orElse(error("Channel " + channelId + " does not exist"))
  }
}
