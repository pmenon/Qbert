package net.qbert.framing.amqp_091

import net.qbert.framing.{ AMQP, AMQArray, AMQFieldTable, AMQLongString, AMQShortString }
import net.qbert.handler.MethodHandler
import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

object AMQP_091 {
  object Connection {
    object Start {
      def apply(fr: FrameReader) = {
        new Start(fr.readOctet, fr.readOctet, fr.readFieldTable, fr.readLongString, fr.readLongString)
      }

      //def unapply(start: Start) = {
      //  Some((start.majorVersion, start.minorVersion, start.serverProperties, start.mechanisms, start.locales))
      //}
      
      //def apply(majorVersion: Int, minorVersion: Int, serverProperties: Map[String, Any], mechanisms: String, locales: String) = {
      //  new Start(majorVersion, minorVersion, serverProperties, mechanisms, locales)
      //}

    }

    private[AMQP_091] case class Start(val majorVersion: Byte, val minorVersion: Byte, val serverProperties: AMQFieldTable, val mechanisms: AMQLongString, val locales: AMQLongString) extends AMQP.Connection.Start {
      val classId = 10
      val methodId = 10

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionStart(channelId, this)

      def argSize = 1 + 1 + serverProperties.size + mechanisms.size + locales.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeOctet(majorVersion)
        fw.writeOctet(minorVersion)
        fw.writeFieldTable(serverProperties)
        fw.writeLongString(mechanisms)
        fw.writeLongString(locales)
      }

      override def toString = "#Connection.Start<majorVersion="+majorVersion+", minorVersion="+minorVersion+">"

    }

    object StartOk {
      def apply(fr: FrameReader) = {
        new StartOk(fr.readFieldTable, fr.readShortString, fr.readLongString, fr.readShortString)
      }

      //def apply(clientProps: Map[String, Any], mechanisms: String, response: String, locales: String) = {
      //  new StartOk(clientProps, mechanisms, response, locales)
      //}
    }

    private[AMQP_091] case class StartOk(val clientProperties: AMQFieldTable, val mechanisms: AMQShortString, val response: AMQLongString, val locales: AMQShortString) extends AMQP.Connection.StartOk {
      val classId = 10
      val methodId = 11

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionStartOk(channelId, this)

      def argSize = clientProperties.size + mechanisms.size + response.size + locales.size

      def writeArguments(fw: FrameWriter) = {}

      override def toString() = "#Connection.StartOk<clientProps="+clientProperties+", mechanisms="+mechanisms+", response="+response+">"

    }

    // Connection.Tune
    object Tune {
      def apply(fr: FrameReader) = {
        new Tune(fr.readShort, fr.readLong, fr.readShort)
      }

      //def apply(channelMax: Int, frameMax: Int, heartbeat: Int) = {
      //  new Tune(channelMax, frameMax, heartbeat)
      //}
    }

    private[AMQP_091] case class Tune(val channelMax: Short, val frameMax: Int, val heartbeat: Short) extends AMQP.Connection.Tune {
      val classId = 10
      val methodId = 30

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionTune(channelId, this)

      def argSize = 2 + 4 + 2

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(channelMax)
        fw.writeLong(frameMax)
        fw.writeShort(heartbeat)
      }

      override def toString() = "#Connection.Tune<channelMax="+channelMax+", frameMax="+frameMax+", heartbeat="+heartbeat+">"

    }

    // Connection.TuneOk
    object TuneOk {
      def apply(fr: FrameReader) = {
        new TuneOk(fr.readShort, fr.readLong, fr.readShort)
      }

      //def apply(channelMax: Int, frameMax: Int, heartbeat: Int) = {
      //  new TuneOk(channelMax, frameMax, heartbeat)
      //}

    }

    private[AMQP_091] case class TuneOk(val channelMax: Short, val frameMax: Int, val heartbeat: Short) extends AMQP.Connection.TuneOk {
      val classId = 10
      val methodId = 31

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionTuneOk(channelId, this)

      def argSize = 2 + 4 + 2

      def writeArguments(fw: FrameWriter) = {}

      override def toString() = "#Connection.TuneOk<channelMax="+channelMax+", frameMax="+frameMax+", heartbeat="+heartbeat+">"

    }

    // Connection.Open
    object Open {
      def apply(fr: FrameReader) = {
        new Open(fr.readShortString, fr.readShortString, fr.readOctet)
      }

      //def apply(virtualHost: String, capabilities: String, insist: Int) = {
      //  new Open(virtualHost, capabilities, insist)
      //}
    }

    private[AMQP_091] case class Open(val virtualHost: AMQShortString, val capabilities: AMQShortString, val insist: Byte) extends AMQP.Connection.Open {
      val classId = 10
      val methodId = 40

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionOpen(channelId, this)

      def argSize = virtualHost.size + capabilities.size + 1

      def writeArguments(fw: FrameWriter) = {}

      override def toString() = "#Connection.Open<virtualHost="+virtualHost+">"

    }
    
    // Connection.OpenOk
    object OpenOk {
      def apply(fr: FrameReader) = {
        new OpenOk(fr.readShortString)
      }

      //def apply(knownHosts: String) = {
      //  new OpenOk(knownHosts)
      //}
    }

    private[AMQP_091] case class OpenOk(val knownHosts: AMQShortString) extends AMQP.Connection.OpenOk {
      val classId = 10
      val methodId = 41

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleConnectionOpenOk(channelId, this)

      def argSize = knownHosts.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShortString(knownHosts)
      }

      override def toString() = "#Connection.Open<virtualHost="+knownHosts+">"

    }
    
  }

  // Channel class
  object Channel {

    // Channel.Open
    object Open {
      def apply(fr: FrameReader) = {
        new Open(fr.readShortString)
      }

      //def apply(outOfBand: String) = {
      //  new OpenOk(outOfBand)
      //}
    }

    private[AMQP_091] case class Open(val outOfBand: AMQShortString) extends AMQP.Channel.Open {
      val classId = 20
      val methodId = 10

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleChannelOpen(channelId, this)

      def argSize = outOfBand.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShortString(outOfBand)
      }

      override def toString() = "#Channel.Open<outOfBand="+outOfBand+">"

    }

    // Channel.OpenOk
    object OpenOk {
      def apply(fr: FrameReader) = {
        new OpenOk(fr.readLongString)
      }

      //def apply(channelId: String) = {
      //  new OpenOk(channelId)
      //}
    }

    private[AMQP_091] case class OpenOk(val channelId: AMQLongString) extends AMQP.Channel.OpenOk {
      val classId = 20
      val methodId = 11

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleChannelOpenOk(channelId, this)

      def argSize = channelId.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeLongString(channelId)
      }

      override def toString() = "#Channel.OpenOk<channelId="+channelId+">"

    }

  }

  // Basic
  object Basic {

    // Basic.Consume
    object Consume extends CanReadFrom[Consume] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = {
        new Consume(fr.readShort, fr.readShortString, fr.readShortString, fr.readOctet, fr.readFieldTable)
      }
    }

    private[AMQP_091] case class Consume(ticket: Short, queueName: AMQShortString, consumerTag: AMQShortString, bitField: Byte, args: AMQFieldTable) extends AMQP.Basic.Consume {
      val classId = 60
      val methodId = 20

      def noLocal = (bitField & (1 << 0)) != 0
      def noAck = (bitField & (1 << 1)) != 0
      def exclusive = (bitField & (1 << 2)) != 0
      def noWait = (bitField & (1 << 3)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleBasicConsume(channelId, this)

      def argSize = 2 + queueName.size + consumerTag.size + 1 + args.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(queueName)
        fw.writeShortString(consumerTag)
        fw.writeOctet(bitField)
        fw.writeFieldTable(args)
      }
    }

    // Basic.Publish
    object Publish extends CanReadFrom[Publish] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = {
        new Publish(fr.readShort, fr.readShortString, fr.readShortString, fr.readOctet)
      }
    }

    private[AMQP_091] case class Publish(ticket: Short, exchangeName: AMQShortString, routingKey: AMQShortString, bitField: Byte) extends AMQP.Basic.Publish {
      val classId = 60
      val methodId = 40

      def mandatory = (bitField & (1 << 0)) != 0
      def immediate = (bitField & (1 << 1)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleBasicPublish(channelId, this)

      def argSize = 2 + exchangeName.size + routingKey.size + 8

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(exchangeName)
        fw.writeShortString(routingKey)
        fw.writeOctet(bitField)
      }

      override def toString() = "#Basic.Publish<exchange="+exchangeName+",routingKey="+routingKey+">"
    }

    // Basic.Deliver
    object Deliver extends CanReadFrom[Deliver] {
      def apply(fr: FrameReader) = readFrom(fr)
      def apply(consumerTag: AMQShortString, deliveryTag: AMQShortString, redelivered: Boolean, exchange: AMQShortString, routingKey: AMQShortString): Deliver = {
        var bitField = 0
        if(redelivered) bitField |= (1 << 0)
        Deliver(consumerTag, deliveryTag, bitField.asInstanceOf[Byte], exchange, routingKey)
      }
      def readFrom(fr: FrameReader) = new Deliver(fr.readShortString, fr.readShortString, fr.readOctet, fr.readShortString, fr.readShortString)
    }

    private[AMQP_091] case class Deliver(consumerTag: AMQShortString, deliveryTag: AMQShortString, bitField: Byte, exchange: AMQShortString, routingKey: AMQShortString) extends AMQP.Basic.Deliver {
      val classId = 60
      val methodId = 60

      def redelivered = (bitField & (1 << 0)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleBasicDeliver(channelId, this)

      def argSize = consumerTag.size + deliveryTag.size + 1 + exchange.size + routingKey.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShortString(consumerTag)
        fw.writeShortString(deliveryTag)
        fw.writeOctet(bitField)
        fw.writeShortString(exchange)
        fw.writeShortString(routingKey)
      }
    }
  }

  object Exchange {
    object Declare extends CanReadFrom[Declare] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new Declare(fr.readShort, fr.readShortString, fr.readShortString, fr.readOctet, fr.readFieldTable)
    }

    private[AMQP_091] case class Declare(ticket: Short, exchangeName: AMQShortString, exchangeType: AMQShortString, bitField: Byte, args: AMQFieldTable) extends AMQP.Exchange.Declare {
      val classId = 40
      val methodId = 10

      def passive = (bitField & (1 << 0)) != 0
      def durable = (bitField & (1 << 1)) != 0
      def autoDelete = (bitField & (1 << 2)) != 0
      def internal = (bitField & (1 << 3)) != 0
      def noWait = (bitField & (1 << 4)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleExchangeDeclare(channelId, this)
      
      def argSize = 2 + exchangeName.size + exchangeType.size + 1 + args.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(exchangeName)
        fw.writeShortString(exchangeType)
        fw.writeOctet(bitField)
        fw.writeFieldTable(args)
      }
    }

    object DeclareOk extends CanReadFrom[DeclareOk] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new DeclareOk
    }

    private[AMQP_091] case class DeclareOk() extends AMQP.Exchange.DeclareOk {
      val classId = 40
      val methodId = 11

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleExchangeDeclareOk(channelId, this)

      def argSize = 0
      
      def writeArguments(fw: FrameWriter) = {}
    }

  }

  // Queue
  object Queue {

    // Queue.Declare
    object Declare extends CanReadFrom[Declare] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new Declare(fr.readShort, fr.readShortString, fr.readOctet, fr.readFieldTable)
    }

    private[AMQP_091] case class Declare(ticket: Short, queueName: AMQShortString, bitField: Byte, args: AMQFieldTable) extends AMQP.Queue.Declare {
      val classId = 50
      val methodId = 10

      def passive = (bitField & (1 << 0)) != 0
      def durable = (bitField & (1 << 1)) != 0
      def exclusive = (bitField & (1 << 2)) != 0
      def autoDelete = (bitField & (1 << 3)) != 0
      def noWait = (bitField & (1 << 4)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleQueueDeclare(channelId, this)

      def argSize = 2 + queueName.size + 1 + args.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(queueName)
        fw.writeOctet(bitField)
        fw.writeFieldTable(args)
      }

      override def toString() = "#Queue.Declare<name="+queueName+",passive="+passive+",durable="+durable+",exclusive="+exclusive+",autoDelete="+autoDelete+",noWait="+noWait+">"
    }

    // Queue.DeclareOk
    object DeclareOk extends CanReadFrom[DeclareOk] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new DeclareOk(fr.readShortString, fr.readLong, fr.readLong)
    }

    private[AMQP_091] case class DeclareOk(queueName: AMQShortString, messageCount: Int, consumerCount: Int) extends AMQP.Queue.DeclareOk {
      val classId = 50
      val methodId = 11

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleQueueDeclareOk(channelId, this)

      def argSize = queueName.size + 4 + 4

      def writeArguments(fw: FrameWriter) = {
        fw.writeShortString(queueName)
        fw.writeLong(messageCount)
        fw.writeLong(consumerCount)
      }

      override def toString() = "#Queue.DeclareOk<name="+queueName+",messageCount="+messageCount+",consumerCount="+consumerCount+">"
    }

    // Queue.Bind
    object Bind extends CanReadFrom[Bind] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new Bind(fr.readShort, fr.readShortString, fr.readShortString, fr.readShortString, fr.readOctet, fr.readFieldTable)
    }

    private[AMQP_091] case class Bind(ticket: Short, queueName: AMQShortString, exchangeName: AMQShortString, routingKey: AMQShortString, bitField: Byte, args: AMQFieldTable) extends AMQP.Queue.Bind {
      val classId = 50
      val methodId = 20

      def noWait = (bitField & (1 << 0)) != 0

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleQueueBind(channelId, this)

      def argSize = 2 + queueName.size + exchangeName.size + routingKey.size + 1 + args.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(queueName)
        fw.writeShortString(exchangeName)
        fw.writeShortString(routingKey)
        fw.writeOctet(bitField)
        fw.writeFieldTable(args)
      }

    }

    object BindOk extends CanReadFrom[BindOk] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = new BindOk
    }

    private[AMQP_091] case class BindOk() extends AMQP.Queue.BindOk {
      val classId = 50
      val methodId = 21

      def handle(channelId: Int, methodHandler: MethodHandler) = methodHandler.handleQueueBindOk(channelId, this)

      def argSize = 0

      def writeArguments(fw: FrameWriter) = {}
    }

  }


}
