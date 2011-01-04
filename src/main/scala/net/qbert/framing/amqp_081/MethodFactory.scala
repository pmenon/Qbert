package net.qbert.framing.amqp_081

import net.qbert.protocol.ProtocolVersion
import net.qbert.framing.{AMQShortString, AMQLongString, AMQFieldTable, MethodFactory}
import net.qbert.network.FrameReader
import net.qbert.util.Logging

class MethodFactory_081 extends MethodFactory with Logging {
  import net.qbert.framing.amqp_091.AMQP_091._

  def createMethodFrom(fr: FrameReader) = None

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