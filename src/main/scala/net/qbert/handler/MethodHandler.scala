package net.qbert.handler

import net.qbert.connection.ConnectionState
import net.qbert.state.StateMachine
import net.qbert.framing.{ AMQP, Frame, Method }
import net.qbert.protocol.AMQProtocolSession

abstract class MethodError(reason: String)
case class UnsupportedMethod(method: String) extends MethodError("Method " + method + " is unsupported by the broker")
case class UnexpectedMethod(method: String) extends MethodError("Method " + method + " was unexpected in current broked state")
case class UnknownVirtualHost(host: String) extends MethodError("VirtualHost " + host + " does not exist")

object MethodHandler {
  def apply(session: AMQProtocolSession) = new SimpleMethodHandler(session)
  def apply(session: AMQProtocolSession, stateMachine: StateMachine[ConnectionState,Method]) = new StateAwareMethodHandlerImpl(session, stateMachine)
}

// 
trait MethodHandler {

  def handleMethod(channelId: Int, method: Method): Either[MethodError, Option[Frame]]
  
  // handle methods that the server should never see up here

  // Connection
  def handleConnectionStart(channelId: Int, start: AMQP.Connection.Start) = error(UnsupportedMethod("Connection.Start"))
  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk): Either[MethodError, Option[Frame]]
  def handleConnectionTune(channelId: Int, tune: AMQP.Connection.Tune) = error(UnsupportedMethod("Connection.Tune"))
  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk): Either[MethodError, Option[Frame]]
  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open): Either[MethodError, Option[Frame]]
  def handleConnectionOpenOk(channelId: Int, connOpenOk: AMQP.Connection.OpenOk) = error(UnsupportedMethod("Connection.OpenOk"))

  // Channel
  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open): Either[MethodError, Option[Frame]]
  def handleChannelOpenOk(channelId: Int, channelOpenOk: AMQP.Channel.OpenOk): Either[MethodError, Option[Frame]] = error(UnsupportedMethod("Channel.OpenOk"))

  // Queue
  def handleQueueDeclare(channelId: Int, declare: AMQP.Queue.Declare): Either[MethodError, Option[Frame]]
  def handleQueueDeclareOk(channelId: Int, declareOk: AMQP.Queue.DeclareOk) = error(UnsupportedMethod("Queue.DeclareOk"))

  // Basic
  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish): Either[MethodError, Option[Frame]]


  protected def success(): Either[MethodError, Option[Frame]] = Right(None)
  protected def success(res: Frame): Either[MethodError, Option[Frame]] = Right(Some(res))
  protected def unexpectedMethodError(method: String): Either[MethodError, Option[Frame]] = Left(UnexpectedMethod(method))
  protected def error[T <: MethodError](m: T): Either[MethodError, Option[Frame]] = Left(m)
}




