package net.qbert.protocol

import net.qbert.broker.QbertBroker
import net.qbert.framing.{ AMQDataBlock, ContentHeader, ContentBody, Frame, AMQFieldTable, AMQLongString, ProtocolInitiation, Method, AMQFrameCodec }
import net.qbert.handler.{MethodHandlingError, MethodHandler }
import net.qbert.network.{ Connection, FrameHandler, FrameReceiver, FrameReader }
import net.qbert.util.Logging
import net.qbert.state.StateMachine


class AMQProtocolDriver(broker: QbertBroker, conn: Connection) extends FrameReceiver with Logging {
  val decoder = AMQFrameCodec.decoder
  var methodHandler: MethodHandler = null
  var handler: FrameHandler = new AMQPConnectionNegotiator(conn, this)

  // This method indicates an error and should close the connection
  def handleError(e: MethodHandlingError) = {
    println("ERROR: " + e)
  }

  // This method writes a response frame on the connection
  def handleResponse(frame: Frame) = {
    conn.writeFrame(frame)
  }

  /**
   * Main entry point for data into the driver.  Depending on the type of of data
   * it either is a protocol initiation or a regular data frame.
   *
   * If the data is a regular frame, it goes through the state manager
   */
  def frameReceived(fr: FrameReader) = handler.frameReceived(fr)

  /*
  override def init(version: ProtocolVersion) = {
    super.init(version)
    methodHandler = MethodHandler(this)
  }
  */
    
  def versionOk(major: Int, minor: Int) = true

  /*
  def protocolInitiation(pi: ProtocolInitiation) = {
    log.info("Protocol Initiation Received ... {}", pi)
    
    val response = if (versionOk(pi.major, pi.minor)) {
      init(ProtocolVersion(pi.major, pi.minor))
      val method = methodFactory.createConnectionStart(protocolVersion, AMQFieldTable(), AMQLongString("AMQPPLAIN"), AMQLongString("en_US"))
      method.generateFrame(0)
    } else {
      ProtocolInitiation(ProtocolInitiation.AMQP_HEADER, 1, 0, 9, 1)
    }
    
    conn writeFrame response
  }

  def methodReceived(channelId: Int, method: Method) = {
    log.info("Method received : {}", method)
    methodHandler.handleMethod(channelId, method)
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
  */

}