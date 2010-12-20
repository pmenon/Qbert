package net.qbert.protocol

import net.qbert.connection.{ AMQConnection, ConnectionState, AwaitingConnectionStartOk, AwaitingConnectionTuneOk, AwaitingConnectionOpen, Opened, Stopped }
import net.qbert.framing.{ AMQP, AMQDataBlock, ContentHeader, ContentBody, Frame, AMQFieldTable, AMQLongString, ProtocolInitiation, Method }
import net.qbert.handler.{MethodHandlingError, UnexpectedMethodHandling, MethodHandler, MethodErrorResponse, MethodSuccessResponse, NoResponseMethodSuccess}
import net.qbert.logging.Logging
import net.qbert.state.StateMachine

class AMQProtocolDriver(val conn: AMQConnection) extends AMQProtocolSession with StateMachine[ConnectionState, Frame] with Logging {
  
  var methodHandler: MethodHandler = null

  // State transitions for an AMQ connection
  when(AwaitingConnectionStartOk){ (frame) => frame.payload match {
    case startOk : AMQP.Connection.StartOk => methodHandler.handleConnectionStartOk(0, startOk) match {
      case MethodErrorResponse(error) => handleError(error); goTo(Stopped)
      case MethodSuccessResponse(res) => handleResponse(res); goTo(AwaitingConnectionTuneOk)
      case NoResponseMethodSuccess => goTo(AwaitingConnectionTuneOk)
    }
    case _ => handleError(UnexpectedMethodHandling("tt")); goTo(Stopped)
  }}

  when(AwaitingConnectionTuneOk){ (frame) => frame.payload match {
    case tuneOk : AMQP.Connection.TuneOk => methodHandler.handleConnectionTuneOk(0, tuneOk) match {
      case MethodErrorResponse(error) => handleError(error); goTo(Stopped)
      case MethodSuccessResponse(res) => handleResponse(res); goTo(AwaitingConnectionOpen)
      case NoResponseMethodSuccess => goTo(AwaitingConnectionOpen)
    }
    case _ => handleError(UnexpectedMethodHandling("tt")); goTo(Stopped)
  }}

  when(AwaitingConnectionOpen){ (frame) => frame.payload match {
    case open: AMQP.Connection.Open => methodHandler.handleConnectionOpen(0, open) match {
      case MethodErrorResponse(error) => handleError(error); goTo(Stopped)
      case MethodSuccessResponse(res) => handleResponse(res); goTo(Opened)
      case NoResponseMethodSuccess => goTo(Opened)
    }
    case _ => handleError(UnexpectedMethodHandling("tt")); goTo(Stopped)
  }}

  when(Opened){ (frame) => frame.payload.typeId match {
    case Frame.FRAME_METHOD =>
      methodReceived(frame.channelId, frame.payload.asInstanceOf[Method]) match {
        case MethodErrorResponse(error) => handleError(error); goTo(Stopped)
        case MethodSuccessResponse(res) => handleResponse(res); stay
        case NoResponseMethodSuccess => stay
      }
    case Frame.FRAME_CONTENT => contentHeaderReceived(frame.channelId, frame.payload.asInstanceOf[ContentHeader]); stay
    case Frame.FRAME_BODY => contentBodyReceived(frame.channelId, frame.payload.asInstanceOf[ContentBody]); stay
  }}

  // The beginning state
  setState(AwaitingConnectionStartOk)

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
  def dataBlockReceived(datablock: AMQDataBlock) = {
    datablock match {
      case pi: ProtocolInitiation => protocolInitiation(pi)
      case frame: Frame => actOn(frame)//connectionStateMachine.actOn(frame)
      case _ => error("unknown frame object received")
    }
  }

  override def init(version: ProtocolVersion) = {
    super.init(version)
    methodHandler = MethodHandler(this)//, connectionStateMachine)
  }
    
  def versionOk(major: Int, minor: Int) = true

  def protocolInitiation(pi: ProtocolInitiation) = {
    info("Protocol Initiation Received ... {}", pi)
    
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
    info("Method received : {}", method)
    methodHandler.handleMethod(channelId, method)
  }

  def contentHeaderReceived(channelId: Int, header: ContentHeader) = {
    info("Content header received : {}", header)
    getChannel(channelId).map( (channel) =>
      channel.publishContentHeader(header)
    ).orElse(error("Channel " + channelId + " does not exist"))
  }

  def contentBodyReceived(channelId: Int, body: ContentBody) = {
    info("Content body received : {} ", body)
    getChannel(channelId).map( (channel) =>
      channel.publishContentBody(body)
    ).orElse(error("Channel " + channelId + " does not exist"))
  }

}