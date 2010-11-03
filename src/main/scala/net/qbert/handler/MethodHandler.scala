package net.qbert.handler

import net.qbert.channel.ChannelManager
import net.qbert.connection.{ AMQConnection, ConnectionState, AwaitingConnectionStartOk, AwaitingConnectionTuneOk, AwaitingConnectionOpen }
import net.qbert.state.{StateAwareProcessor, StateMachine}
import net.qbert.message.MessagePublishInfo
import net.qbert.framing.{AMQP, Frame, Method, MethodFactory, AMQLongString, AMQShortString}
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolSession
import net.qbert.virtualhost.VirtualHostRegistry

case class MethodError(reason: String)

object MethodHandler {
  def apply(session: AMQProtocolSession) = new SimpleMethodHandler(session)
  def apply(session: AMQProtocolSession, stateMachine: StateMachine[ConnectionState,Method]) = new StateAwareMethodHandlerImpl(session, stateMachine)
}

// 
trait MethodHandler {
  def handleMethod(channelId: Int, method: Method): Either[MethodError, Option[Frame]] 
  
  
  // handle methods that the server should never see up here
  def handleConnectionStart(channelId: Int, start: AMQP.Connection.Start): Either[MethodError, Option[Frame]] = Left(MethodError("Qbert broker doesn't support this method"))
  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk): Either[MethodError, Option[Frame]]
  def handleConnectionTune(channelId: Int, tune: AMQP.Connection.Tune): Either[MethodError, Option[Frame]] = Left(MethodError("Qbert broker doesn't support this method"))
  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk): Either[MethodError, Option[Frame]]
  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open): Either[MethodError, Option[Frame]]
  def handleConnectionOpenOk(channelId: Int, connOpenOk: AMQP.Connection.OpenOk): Either[MethodError, Option[Frame]] = Left(MethodError("Qbert broker doesn't support this method"))
  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open): Either[MethodError, Option[Frame]]
  def handleChannelOpenOk(channelId: Int, channelOpenOk: AMQP.Channel.OpenOk): Either[MethodError, Option[Frame]] = Left(MethodError("Qbert broker doesn't support this method"))
  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish): Either[MethodError, Option[Frame]]

  protected def success(): Either[MethodError, Option[Frame]] = success(None)
  protected def success(res: Option[Frame]): Either[MethodError, Option[Frame]] = Right(res)
  protected def error(s: String): Either[MethodError, Option[Frame]] = Left(MethodError(s))
}

class SimpleMethodHandler(val session: AMQProtocolSession) extends MethodHandler with Logging {

  def handleMethod(channelId: Int, method: Method) = method.handle(channelId, this)

  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk) = {
    info("Connection.Start received: " + startOk)

    val tuneMessage = session.methodFactory.createConnectionTune(100,100,100)
    val response = tuneMessage.generateFrame(0)
    
    success(Some(response))
  }


  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk) = {
    info("Connection.TuneOk received: " + tuneOk)
    // do nothing ... for now
    success()
  }

  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open) = {
    info("Connection.Open received: " + connOpen)

    session.virtualHost = VirtualHostRegistry.get(connOpen.virtualHost.get)
    val openok = session.methodFactory.createConnectionOpenOk(AMQShortString(""))
    val response = openok.generateFrame(0)

    success(Some(response))
  }

  // TODO: Needs to go !
  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open) = {
    info("Channel.Open received: " + channelOpen)

    val c = session createChannel channelId 
    //val openok = AMQP.Channel.OpenOk(AMQLongString("queue-"+c.channelId.toString))
    val openok = session.methodFactory.createChannelOpenOk(AMQLongString("queue-"+c.channelId.toString))
    val response = openok.generateFrame(channelId)
    
    success(Some(response))
  }

  // TODO: Needs to go
  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish) = {
    val publishInfo = MessagePublishInfo(publish.exchangeName.get, publish.routingKey.get, publish.mandatory, publish.immediate)
    session.getChannel(channelId).map{_.publishReceived(publishInfo)}.getOrElse {
      info("Channel {} does not exist during basic.publish attempt")
      error("Channel doesn't not exist")
    }
    success()
  }

}

trait StateAwareMethodHandler extends MethodHandler with StateAwareProcessor[ConnectionState] {
  abstract override def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk) = {
    when(AwaitingConnectionStartOk)(super.handleConnectionStartOk(channelId, startOk))(error("StartOK method was unexpected in current broker state"))
  }
  abstract override def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk) = {
    when(AwaitingConnectionTuneOk)(super.handleConnectionTuneOk(channelId, tuneOk))(error("TuneOk method was unexpected in current broker state"))
  }
  abstract override def handleConnectionOpen(channelId: Int, open: AMQP.Connection.Open) = {
    when(AwaitingConnectionOpen)(super.handleConnectionOpen(channelId, open))(error("Connection.Open method was unexpected in current broker state"))
  }
}

class StateAwareMethodHandlerImpl(session: AMQProtocolSession, val stateMachine: StateMachine[ConnectionState, Method]) extends SimpleMethodHandler(session) with StateAwareMethodHandler {

}
