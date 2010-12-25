package net.qbert.framing

import amqp_081.MethodFactory_081
import amqp_091.MethodFactory_091
import net.qbert.protocol.ProtocolVersion
import net.qbert.network.{ CanReadIn, FrameReader }

object MethodFactory {
  private val map = Map((0,9) -> new MethodFactory_091,
                        (0,8) -> new MethodFactory_081)
  def createWithVersion(pv: ProtocolVersion): MethodFactory = map.get((pv.major, pv.minor)).getOrElse {
    error("There is no factory for protocol version: " + pv)
    map.get((0,9)).get
  }
}

trait MethodFactory extends CanReadIn[Option[Method]] {
  def createMethodFrom(fr: FrameReader): Option[Method]
  def readFrom(fr: FrameReader) = createMethodFrom(fr)

  def createConnectionStart(version: ProtocolVersion, props: AMQFieldTable, mechanisms: AMQLongString, locales: AMQLongString): AMQP.Connection.Start
  def createConnectionTune(channelMax: Short, frameMax: Int, heartbeat: Short): AMQP.Connection.Tune
  def createConnectionOpenOk(knownHosts: AMQShortString): AMQP.Connection.OpenOk
  def createConnectionClose(replyCode: Int, replyText: AMQShortString, classId: Int, methodId: Int): AMQP.Connection.Close
  def createChannelOpenOk(channelId: AMQLongString): AMQP.Channel.OpenOk
  def createExchangeDeclareOk(): AMQP.Exchange.DeclareOk
  def createQueueDeclareOk(queueName: AMQShortString, messageCount: Int, consumerCount: Int): AMQP.Queue.DeclareOk
  def createQueueBindOk(): AMQP.Queue.BindOk
  def createBasicDeliver(consumerTag: AMQShortString, deliveryTag: Long, redelivered: Boolean, exchange: AMQShortString, routingKey: AMQShortString): AMQP.Basic.Deliver
  def createBasicConsumeOk(consumerTag: AMQShortString): AMQP.Basic.ConsumeOk
}
