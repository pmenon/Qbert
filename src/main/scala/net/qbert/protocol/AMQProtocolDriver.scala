package net.qbert.protocol

import net.qbert.channel.{ ChannelManager }
import net.qbert.connection.AMQConnection
import net.qbert.framing.{ AMQDataBlock, Frame, AMQP, FramePayload, ProtocolInitiation, Method, FieldTable, AMQLongString }
import net.qbert.handler.StateAwareMethodHandler
import net.qbert.logging.Logging
import net.qbert.state.{ State, StateManager }
import net.qbert.virtualhost.AMQVirtualHost

trait StateAware {
  val stateManager = new StateManager
}

trait AMQProtocolSession extends ChannelManager {
  val conn: AMQConnection
  var protocolVersion: Option[ProtocolVersion] = None
  var virtualHost: Option[AMQVirtualHost] = None

  def virtualHost_=(host: AMQVirtualHost) = Some(host)

  def writeFrame(frame: Frame) = conn writeFrame frame
}

class AMQProtocolDriver(val conn: AMQConnection) extends AMQProtocolSession with StateAware with Logging {

  //val stateManager = new StateManager
  val methodHandler = new StateAwareMethodHandler(this)

  def dataBlockReceived(datablock: AMQDataBlock) = {
    datablock match {
      case pi: ProtocolInitiation => protocolInitiation(pi)
      case frame: Frame => frameReceived(frame)
      case _ => error("unknown frame object received")
    }
  }

  def versionOk(major: Int, minor: Int) = true

  def protocolInitiation(pi: ProtocolInitiation) = {
    if(stateManager notInState State.waitingConnection) error("incorrect order")

    info("Protocol Initiation Received ..." + pi)
    
    val response = if (versionOk(pi.major, pi.minor)) {
      val method = AMQP.Connection.Start(0, 9, FieldTable(), AMQLongString("AMQPPLAIN"), AMQLongString("en_US"))
      method.generateFrame(0)
    } else {
      ProtocolInitiation(ProtocolInitiation.AMQP_HEADER, 1, 1, 0, 9)
    }
    
    conn writeFrame response

    stateManager nextNaturalState
  }

  def frameReceived(frame: Frame) = {
    info("Method received : " + frame.payload)
    info("Channel = " + frame.channelId)
    methodHandler.handleMethod(frame.channelId, frame.payload)
  }


}
