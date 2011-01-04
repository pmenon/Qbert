package net.qbert.protocol

import net.qbert.connection.{ AMQConnection, ConnectionProperties }
import net.qbert.framing.{ AMQP, AMQDataBlock, AMQFrameCodec, AMQFieldTable, AMQLongString, AMQShortString, Frame, ProtocolInitiation, MethodFactory }
import net.qbert.network.{ Connection, FrameHandler }
import net.qbert.state.StateMachine
import net.qbert.util.Logging
import net.qbert.virtualhost.AMQVirtualHost

/**
 * AMQPConnectionNegotiator
 *
 * @author: <a href="www.prashanthmenon.com">Prashanth Menon</a>
 *
 * @version: 11-01-02 11:38 PM (02 01 2011)
 */

sealed abstract class ConnectionState
object ConnectionState {
  case object AwaitingProtocolInitiation extends ConnectionState
  case object AwaitingConnectionStartOk extends ConnectionState
  case object AwaitingConnectionTuneOk extends ConnectionState
  case object AwaitingConnectionOpen extends ConnectionState
  case object Opened extends ConnectionState
  case object Closing extends ConnectionState
  case object Stopped extends ConnectionState
}

class AMQPConnectionNegotiator(val conn: Connection, val driver: AMQProtocolDriver) extends FrameHandler with StateMachine[ConnectionState, AMQDataBlock] with Logging {
  import ConnectionState._

  var version: ProtocolVersion = _
  val decoder = AMQFrameCodec.decoder
  lazy val methodFactory = MethodFactory.createWithVersion(version)

  var maxChannels: Int = _
  var maxFrameSize: Int = _
  var heartbeat: Int = _
  var virtualHost: AMQVirtualHost = _

  def handleFrame(frame: AMQDataBlock) = actOn(frame)

  // State transitions for an AMQ connection
  when(AwaitingProtocolInitiation){
    case protocolVersion: ProtocolInitiation => handleProtocolInitiation(protocolVersion)
    case _ => handleError(); goTo(Stopped)
  }

  when(AwaitingConnectionStartOk){
    case frame: Frame => frame.payload match {
      case startOk: AMQP.Connection.StartOk => handleConnectionStartOk(startOk)
    }
    case _ => handleError(); goTo(Stopped)
  }

  when(AwaitingConnectionTuneOk){
    case frame: Frame => frame.payload match {
      case tuneOk : AMQP.Connection.TuneOk => handleConnectionTuneOk(tuneOk)
    }
    case _ => handleError(); goTo(Stopped)
  }

  when(AwaitingConnectionOpen){
    case frame: Frame => frame.payload match {
      case open: AMQP.Connection.Open => handleConnectionOpen(open)
    }
    case _ => handleError(); goTo(Stopped)
  }

  // The beginning state
  setState(AwaitingProtocolInitiation)

  def handleError() = {
    val errorCode = 402
    val errorString = "Unexpected method received"
    val method = methodFactory.createConnectionClose(errorCode, errorString, 0, 0)
    conn.writeFrame(method.generateFrame(0))
  }

  def handleProtocolInitiation(pi: ProtocolInitiation) = {
    log.info("ProtocolInitiation frame received: {}", pi)
    // check if the protocol version is good
    if (driver.versionOk(pi.major, pi.minor)) {
      // set the version for the connection
      version = ProtocolVersion(pi.major, pi.minor)

      // set the protocol version the decoder should be using
      decoder.setVersion(version)

      // create a connection start method to respond to the client
      val method = methodFactory.createConnectionStart(version, AMQFieldTable(), AMQLongString("AMQPPLAIN"), AMQLongString("en_US"))

      // write the frame on the connection
      conn.writeFrame(method.generateFrame(0))

      // move the state to awaiting a StartOk method
      goTo(AwaitingConnectionStartOk)
    } else {
      // the version if not correct, we send a proper version and close the connection
      conn.writeFrame(ProtocolInitiation(ProtocolInitiation.AMQP_HEADER, 1, 0, 9, 1))
      goTo(AwaitingConnectionStartOk)
    }
  }

  def handleConnectionStartOk(startOk: AMQP.Connection.StartOk) = {
    log.info("Connection.Start received: {}", startOk)

    // create a connection tune message
    val tuneMessage = methodFactory.createConnectionTune(100,100,100)

    // write the message out
    conn.writeFrame(tuneMessage.generateFrame(0))

    // switch state to awaiting connection tune ack
    goTo(AwaitingConnectionTuneOk)
  }

  def handleConnectionTuneOk(tuneOk: AMQP.Connection.TuneOk) = {
    log.info("Connection.TuneOk received: {}", tuneOk)
    // do nothing ... for now
    goTo(AwaitingConnectionOpen)
  }

  def handleConnectionOpen(connOpen: AMQP.Connection.Open) = {
    log.info("Connection.Open received: {}", connOpen)

    // create an Open.Ok response and send it on the connection
    val openok = methodFactory.createConnectionOpenOk(AMQShortString(""))
    conn.writeFrame(openok.generateFrame(0))

    // create connection properties and an AMQConnection to handle any further frames on this connection
    val connectionProperties = new ConnectionProperties(version, maxChannels, maxFrameSize, heartbeat)
    val connection = new AMQConnection(conn, decoder, connectionProperties, virtualHost)

    // set the connection to receive frames from the driver
    driver.handler = connection

    // open the connection
    goTo(Opened)
  }
}