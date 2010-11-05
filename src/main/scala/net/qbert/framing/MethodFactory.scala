package net.qbert.framing

import net.qbert.logging.Logging
import net.qbert.protocol.ProtocolVersion
import net.qbert.network.{ CanReadFrom, FrameReader }

object MethodFactory {
  private val map = Map((0,9) -> new MethodFactory_091,
                        (0,8) -> new MethodFactory_081)
  def createWithVersion(pv: ProtocolVersion): MethodFactory = map.get((pv.major, pv.minor)).getOrElse {
    error("There is no factory for protocol version: " + pv)
    map.get((0,9)).get
  }
}

trait MethodFactory extends CanReadFrom[Option[Method]] {
  def createMethodFrom(fr: FrameReader): Option[Method]
  def readFrom(fr: FrameReader) = createMethodFrom(fr)

  def createConnectionStart(version: ProtocolVersion, props: AMQFieldTable, mechanisms: AMQLongString, locales: AMQLongString): AMQP.Connection.Start
  def createConnectionTune(channelMax: Short, frameMax: Int, heartbeat: Short): AMQP.Connection.Tune
  def createConnectionOpenOk(knownHosts: AMQShortString): AMQP.Connection.OpenOk
  def createChannelOpenOk(channelId: AMQLongString): AMQP.Channel.OpenOk
  def createExchangeDeclareOk(): AMQP.Exchange.DeclareOk
  def createQueueDeclareOk(queueName: AMQShortString, messageCount: Int, consumerCount: Int): AMQP.Queue.DeclareOk
  def createQueueBindOk(): AMQP.Queue.BindOk
}

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

      case (60, 40) => Some(Basic.Publish(fr))

      case _ => info("No method matches classId={} methodId={}", classId, methodId); None
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
  
}

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
}
