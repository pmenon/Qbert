package net.qbert.framing.amqp_091

import net.qbert.network.FrameReader
import net.qbert.framing.{AMQShortString, AMQLongString, AMQFieldTable, MethodFactory}
import net.qbert.protocol.ProtocolVersion
import net.qbert.logging.Logging

class MethodFactory_091 extends MethodFactory with Logging {
  import net.qbert.framing.amqp_091.AMQP_091._

  def createMethodFrom(fr: FrameReader) = {
    val classId = fr.readShort
    val methodId = fr.readShort

    val method = (classId, methodId) match {
      case (10, 10) => Some(Connection.Start(fr))
      case (10, 11) => Some(Connection.StartOk(fr))
      case (10, 30) => Some(Connection.Tune(fr))
      case (10, 31) => Some(Connection.TuneOk(fr))
      case (10, 40) => Some(Connection.Open(fr))
      case (10, 41) => Some(Connection.OpenOk(fr))

      case (20, 10) => Some(Channel.Open(fr))
      case (20, 11) => Some(Channel.OpenOk(fr))

      case (40, 10) => Some(Exchange.Declare(fr))
      case (40, 11) => Some(Exchange.DeclareOk(fr))

      case (50, 10) => Some(Queue.Declare(fr))
      case (50, 11) => Some(Queue.DeclareOk(fr))
      case (50, 20) => Some(Queue.Bind(fr))
      case (50, 21) => Some(Queue.BindOk(fr))

      case (60, 20) => Some(Basic.Consume(fr))
      case (60, 40) => Some(Basic.Publish(fr))

      case _ => logInfo("No method matches classId={} methodId={}", classId, methodId); None
    }

    method
  }

  def createConnectionStart(version: ProtocolVersion, props: AMQFieldTable, mechanisms: AMQLongString, locales: AMQLongString) = {
    Connection.Start(version.major, version.minor, props, mechanisms, locales)
  }

  def createConnectionTune(channelMax: Short, frameMax: Int, heartbeat: Short) = {
    Connection.Tune(channelMax, frameMax, heartbeat)
  }

  def createConnectionOpenOk(knownHosts: AMQShortString) = {
    Connection.OpenOk(knownHosts)
  }

  def createConnectionClose(replyCode: Int, replyText: AMQShortString, errClassId: Int, errMethodId: Int) = {
    Connection.Close(replyCode, replyText, errClassId, errMethodId)
  }

  def createChannelOpenOk(channelId: AMQLongString) = {
    Channel.OpenOk(channelId)
  }

  def createExchangeDeclareOk() = {
    Exchange.DeclareOk()
  }

  def createQueueDeclareOk(queueName: AMQShortString, messageCount: Int, consumerCount: Int) = {
    Queue.DeclareOk(queueName, messageCount, consumerCount)
  }

  def createQueueBindOk() = {
    Queue.BindOk()
  }

  def createBasicDeliver(consumerTag: AMQShortString, deliveryTag: Long, redelivered: Boolean, exchange: AMQShortString, routingKey: AMQShortString) = {
    Basic.Deliver(consumerTag, deliveryTag, redelivered, exchange, routingKey)
  }

  def createBasicConsumeOk(consumerTag: AMQShortString) = {
    Basic.ConsumeOk(consumerTag)
  }

}