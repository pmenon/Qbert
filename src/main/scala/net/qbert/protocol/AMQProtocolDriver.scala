package net.qbert.protocol

import net.qbert.connection.AMQConnection
import net.qbert.framing.{ AMQDataBlock, Frame, AMQFieldTable, AMQLongString, ProtocolInitiation, Method, MethodFactory }
import net.qbert.handler.MethodHandler
import net.qbert.logging.Logging
import net.qbert.state.{ State, StateDriven, StateManager }

trait AMQProtocolDriver extends AMQProtocolSession with Logging {
  def dataBlockReceived(datablock: AMQDataBlock) = {
    datablock match {
      case pi: ProtocolInitiation => protocolInitiation(pi)
      case frame: Frame => frameReceived(frame)
      case _ => error("unknown frame object received")
    }
  }

  def protocolInitiationReceived(pi: ProtocolInitiation): Unit
  def frameReceived(f: Frame): Unit
}


class StateDrivenAMQProtocolDriver(val conn: AMQConnection) extends AMQProtocolDriver with StateMachine with Logging {
  val initialState = State.waitingConnection
  var methodHandler: MethodHandler = null

  override def init(version: ProtocolVersion) = {
    super.init(version)
    methodHandler = MethodHandler(this)
  }
    
  def versionOk(major: Int, minor: Int) = true

  /*
  def protocolInitiation(pi: ProtocolInitiation) = {
    if(stateManager notInState State.waitingConnection) error("incorrect order")

    info("Protocol Initiation Received ..." + pi)
    
    val response = if (versionOk(pi.major, pi.minor)) {
      init(ProtocolVersion(pi.major, pi.minor))
      val method = methodFactory.createConnectionStart(protocolVersion, AMQFieldTable(), AMQLongString("AMQPPLAIN"), AMQLongString("en_US"))
      method.generateFrame(0)
    } else {
      ProtocolInitiation(ProtocolInitiation.AMQP_HEADER, 1, 0, 9, 1)
    }
    
    conn writeFrame response

    stateManager nextNaturalState
  }

  def frameReceived(frame: Frame) = {
    info("Method received : " + frame.payload)
    methodHandler.handleMethod(frame.channelId, frame.payload.asInstanceOf[Method])
  }
  */


}
