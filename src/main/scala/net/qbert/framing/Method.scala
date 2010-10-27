package net.qbert.framing

import net.qbert.framing.Frame
import net.qbert.handler.MethodHandler
import net.qbert.network.{FrameWriter, FrameReader}

trait Method extends FramePayload {
  val typeId = Frame.FRAME_METHOD

  val classId: Int 
  val methodId: Int

  def argSize(): Int
  def writeArguments(fw: FrameWriter): Unit
  def handle(methodHandler: MethodHandler): Unit

  def writeTo(fw: FrameWriter) = {
    fw.writeShort(classId)
    fw.writeShort(methodId)
    writeArguments(fw)
  }

  def size() = 2 + 2 + argSize

  def generateFrame(channelId: Int): Frame = {
    Frame(typeId, channelId, this)
  }
}

object AMQP {
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

    private[AMQP] case class Start(val majorVersion: Byte, val minorVersion: Byte, val serverProperties: FieldTable, val mechanisms: AMQLongString, val locales: AMQLongString) extends Method {
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

    private[AMQP] case class StartOk(val clientProps: FieldTable, val mechanisms: AMQShortString, val response: AMQLongString, val locales: AMQShortString) extends Method {
      val classId = 10
      val methodId = 11

      def handle(methodHandler: MethodHandler) = methodHandler.handleConnectionStartOk(this)

      def argSize = clientProps.size + mechanisms.size + response.size + locales.size

      def writeArguments(fw: FrameWriter) = {}

      override def toString() = "#Connection.StartOk<clientProps="+clientProps+", mechanisms="+mechanisms+", response="+response+">"

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

    private[AMQP] case class Tune(val channelMax: Short, val frameMax: Int, val heartbeat: Short) extends Method {
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

    private[AMQP] case class TuneOk(val channelMax: Short, val frameMax: Int, val heartbeat: Short) extends Method {
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

    private[AMQP] case class Open(val virtualHost: AMQShortString, val capabilities: AMQShortString, val insist: Byte) extends Method {
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

    private[AMQP] case class OpenOk(val knownHosts: AMQShortString) extends Method {
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

    private[AMQP] case class Open(val outOfBand: AMQShortString) extends Method {
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

    private[AMQP] case class OpenOk(val channelId: AMQLongString) extends Method {
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

}






