package net.qbert.protocol

import net.qbert.connection.{ AMQConnection, ConnectionState, AwaitingConnectionStartOk, AwaitingConnectionTuneOk, AwaitingConnectionOpen, Opened, Stopped }
import net.qbert.framing.{ AMQDataBlock, ContentHeader, ContentBody, Frame, AMQFieldTable, AMQLongString, ProtocolInitiation, Method, MethodFactory }
import net.qbert.handler.{MethodError, MethodHandler}
import net.qbert.logging.Logging
import net.qbert.state.StateMachine

class AMQProtocolDriver(val conn: AMQConnection) extends AMQProtocolSession with Logging {
  
  var methodHandler: MethodHandler = null
  val connectionStateMachine = new ConnectionStateMachine
  var frameDispatcher: Function1[Frame, Unit] = dispatchToConnectionStateMachine

  def dispatchToConnectionStateMachine(f: Frame) = {
    connectionStateMachine.actOn(f.payload.asInstanceOf[Method])
  }

  def dispatchToHandler(f: Frame) = {
    f.typeId match {
      case Frame.FRAME_METHOD => methodReceived(f.channelId, f.payload.asInstanceOf[Method])
      case Frame.FRAME_CONTENT => contentHeaderReceived(f.channelId, f.payload.asInstanceOf[ContentHeader])
      case Frame.FRAME_BODY => contentBodyReceived(f.channelId, f.payload.asInstanceOf[ContentBody])
    }
  }

  class ConnectionStateMachine extends StateMachine[ConnectionState, Method] {
    def handleWithSuccess(method: Method, successState: ConnectionState) = {
      methodHandler.handleMethod(0, method) match {
        case Left(error) => handleError(error)
        case Right(res) => handleResponse(res); println("moving to " + successState); goTo(successState)
      }
    }

    when(AwaitingConnectionStartOk){(method) => handleWithSuccess(method, AwaitingConnectionTuneOk)}

    when(AwaitingConnectionTuneOk){ (method) => handleWithSuccess(method, AwaitingConnectionOpen) }

    when(AwaitingConnectionOpen){ (method) => methodHandler.handleMethod(0, method) match {
      case Left(error) => handleError(error)
      case Right(res) => handleResponse(res); openConnection(); goTo(Opened)
    }}

    setState(AwaitingConnectionStartOk)
  }

  def handleError(e: MethodError) = {
    println("ERROR: " + e)
    Stopped
  }

  def handleResponse(frame: Option[Frame]) = {
    frame.foreach(conn.writeFrame(_))
  }

  def openConnection() = {
    frameDispatcher = dispatchToHandler
    println("opened!")
  }

  def dataBlockReceived(datablock: AMQDataBlock) = {
    datablock match {
      case pi: ProtocolInitiation => protocolInitiation(pi)
      case frame: Frame => frameDispatcher(frame)
      case _ => error("unknown frame object received")
    }
  }

  override def init(version: ProtocolVersion) = {
    super.init(version)
    methodHandler = MethodHandler(this, connectionStateMachine)
  }
    
  def versionOk(major: Int, minor: Int) = true

  def protocolInitiation(pi: ProtocolInitiation) = {


    info("Protocol Initiation Received ..." + pi)
    
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
    info("Method received : " + method)
    methodHandler.handleMethod(channelId, method) match {
      case Left(error) => handleError(error)
      case Right(res) => handleResponse(res)
    }
  }

  def contentHeaderReceived(channelId: Int, header: ContentHeader) = {}

  def contentBodyReceived(channelId: Int, body: ContentBody) = {}


}
