package net.qbert.handler

import net.qbert.error.{ QbertError, GenericQbertResponse }
import net.qbert.framing.{ AMQP, Frame, Method }
import net.qbert.protocol.AMQProtocolSession

abstract class MethodError(reason: String) extends QbertError
case class UnsupportedMethod(method: String) extends MethodError("Method " + method + " is unsupported by the broker")
case class UnexpectedMethod(method: String) extends MethodError("Method " + method + " was unexpected in current broker state")
case class UnknownVirtualHost(host: String) extends MethodError("VirtualHost " + host + " does not exist")
case class QueueDoesNotExist(queue: String) extends MethodError("Queue with name " + queue + " does not exist")
case class ExchangeDoesNotExist(exchange: String) extends MethodError("Exchange with name " + exchange + " does not exist")

// The response from any method handler
sealed abstract class MethodHandlerResponse extends GenericQbertResponse
case class MethodErrorResponse(error: MethodError) extends MethodHandlerResponse
case object NoResponseMethodSuccess extends MethodHandlerResponse
case class MethodSuccessResponse(res: Frame) extends MethodHandlerResponse

object MethodHandler {

  def apply(session: AMQProtocolSession) = new SimpleMethodHandler(session)

}

// 
trait MethodHandler {

  // main entry point
  def handleMethod(channelId: Int, method: Method): MethodHandlerResponse

  // Connection
  def handleConnectionStart(channelId: Int, start: AMQP.Connection.Start) = error(UnsupportedMethod("Connection.Start"))
  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk): MethodHandlerResponse
  def handleConnectionTune(channelId: Int, tune: AMQP.Connection.Tune) = error(UnsupportedMethod("Connection.Tune"))
  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk): MethodHandlerResponse
  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open): MethodHandlerResponse
  def handleConnectionOpenOk(channelId: Int, connOpenOk: AMQP.Connection.OpenOk) = error(UnsupportedMethod("Connection.OpenOk"))

  // Channel
  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open): MethodHandlerResponse
  def handleChannelOpenOk(channelId: Int, channelOpenOk: AMQP.Channel.OpenOk) = error(UnsupportedMethod("Channel.OpenOk"))

  // Exchange
  def handleExchangeDeclare(channelId: Int, declare: AMQP.Exchange.Declare): MethodHandlerResponse
  def handleExchangeDeclareOk(channelId: Int, declareOk: AMQP.Exchange.DeclareOk) = error(UnsupportedMethod("Exchange.DeclareOk"))

  // Queue
  def handleQueueDeclare(channelId: Int, declare: AMQP.Queue.Declare): MethodHandlerResponse
  def handleQueueDeclareOk(channelId: Int, declareOk: AMQP.Queue.DeclareOk) = error(UnsupportedMethod("Queue.DeclareOk"))
  def handleQueueBind(channelId: Int, bind: AMQP.Queue.Bind): MethodHandlerResponse
  def handleQueueBindOk(channelId: Int, bindOk: AMQP.Queue.BindOk) = error(UnsupportedMethod("Queue.BindOk"))

  // Basic
  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish): MethodHandlerResponse
  def handleBasicDeliver(channelId: Int, deliver: AMQP.Basic.Deliver) = error(UnsupportedMethod("Basic.Deliver"))


  protected def success(): MethodHandlerResponse = NoResponseMethodSuccess
  protected def success(res: Frame): MethodHandlerResponse = MethodSuccessResponse(res)
  protected def unexpectedMethodError(method: String): MethodHandlerResponse = MethodErrorResponse(UnexpectedMethod(method))
  protected def error[T <: MethodError](m: T): MethodHandlerResponse = MethodErrorResponse(m)
}




