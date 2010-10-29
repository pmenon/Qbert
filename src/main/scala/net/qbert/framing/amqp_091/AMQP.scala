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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionStart(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionStartOk(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionTune(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionTuneOk(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionOpen(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionOpenOk(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleChannelOpen(this)

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

      def handle(methodHandler: MethodHandler) = methodHandler.handleChannelOpenOk(this)

      def argSize = channelId.size

      def writeArguments(fw: FrameWriter) = {
        fw.writeLongString(channelId)
      }

      override def toString() = "#Channel.OpenOk<channelId="+channelId+">"

    }

  }

  object Basic {
    object Publish extends CanReadFrom[Publish] {
      def apply(fr: FrameReader) = readFrom(fr)
      def readFrom(fr: FrameReader) = {
        new Publish(fr.readShort, fr.readShortString, fr.readShortString, fr.readOctet)
      }
    }

    private[AMQP_091] case class Publish(val ticket: Short, exchangeName: AMQShortString, routingKey: AMQShortString, bitField: Byte) extends AMQP.Basic.Publish {
      val classId = 60
      val methodId = 40

      def mandatory = (bitField & 0x1) != 0
      def immediate = (bitField & 0x2) != 0

      def handle(methodHandler: MethodHandler) = methodHandler.handleBasicPublish(this)

      def argSize = 2 + exchangeName.size + routingKey.size + 8

      def writeArguments(fw: FrameWriter) = {
        fw.writeShort(ticket)
        fw.writeShortString(exchangeName)
        fw.writeShortString(routingKey)
        fw.writeOctet(bitField)
      }

      override def toString() = "#Basic.Publish<exchange="+exchangeName+",routingKey="+routingKey+">"
    }
  }

}