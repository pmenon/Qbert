package net.qbert.protocol

import net.qbert.channel.{ ChannelManager }
import net.qbert.connection.AMQConnection
import net.qbert.framing.{ AMQDataBlock, Frame, AMQFieldTable, AMQLongString, AMQShortString, AMQP, FramePayload, ProtocolInitiation, Method, MethodFactory }
import net.qbert.handler.MethodHandler
import net.qbert.logging.Logging
import net.qbert.state.{ State, StateDriven, StateManager }
import net.qbert.virtualhost.AMQVirtualHost

trait AMQProtocolSession extends ChannelManager {
  val conn: AMQConnection
  var protocolVersion: ProtocolVersion = null
  var virtualHost: Option[AMQVirtualHost] = None
  var methodFactory: MethodFactory = null

  def virtualHost_=(host: AMQVirtualHost) = Some(host)

  def writeFrame(frame: Frame) = conn writeFrame frame

  def init(pv: ProtocolVersion) = {
    protocolVersion = pv
    methodFactory = MethodFactory.createWithVersion(protocolVersion)
  }
}

class AMQProtocolDriver(val conn: AMQConnection) extends AMQProtocolSession with StateDriven with Logging {
  val initialState = State.waitingConnection
  var methodHandler: MethodHandler = null

  def dataBlockReceived(datablock: AMQDataBlock) = {
    datablock match {
      case pi: ProtocolInitiation => protocolInitiation(pi)
      case frame: Frame => frameReceived(frame)
      case _ => error("unknown frame object received")
    }
  }

  override def init(version: ProtocolVersion) = {
    super.init(version)
    methodHandler = MethodHandler(this)
  }
    
  def versionOk(major: Int, minor: Int) = true

  def protocolInitiation(pi: ProtocolInitiation) = {
    if(stateManager notInState State.waitingConnection) error("incorrect order")

    info("Protocol Initiation Received ..." + pi)
    
    val response = if (versionOk(pi.major, pi.minor)) {
      init(ProtocolVersion(pi.major, pi.minor))
      val method = methodFactory.createConnectionStart(protocolVersion, AMQFieldTable(), AMQLongString("AMQPPLAIN"), AMQLongString("en_US"))
      method.generateFrame(0)
    } else {
      ProtocolInitiation(ProtocolInitiation.AMQP_HEADER, 1, 1, 0, 9)
    }
    
    conn writeFrame response

    stateManager nextNaturalState
  }

  def frameReceived(frame: Frame) = {
    info("Method received : " + frame.payload)
    methodHandler.handleMethod(frame.channelId, frame.payload.asInstanceOf[Method])
  }


}
