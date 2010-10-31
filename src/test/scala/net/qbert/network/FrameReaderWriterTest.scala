package net.qbert.network

import net.qbert.framing.{AMQP, AMQType, AMQFrameDecoderImpl, Frame, Method, AMQFieldTable, AMQShortString, ShortStringType}
import net.qbert.framing.amqp_091.AMQP_091
import net.qbert.protocol.ProtocolVersion

import org.specs._

class FrameReaderWriterTest extends Specification {
  "FrameReader should" should {
    "properly parse method frames" in {
      
      val props = Map(AMQShortString("test") -> ShortStringType("Qbert"))
      val m = AMQP_091.Connection.Start(0,9,AMQFieldTable(props), "simple", "en_US")

      val frame = m.generateFrame(0)
      val fw = new FrameWriter(frame.size)
      frame writeTo fw

      val fr = new FrameReader(fw.frame)
      val decoder = new AMQFrameDecoderImpl(ProtocolVersion(0,9))

      val frame2 = decoder.decode(fr)

      frame2 must beSome[Frame].which(_.payload.isInstanceOf[AMQP.Connection.Start])
      val m1 = frame2.get.payload.asInstanceOf[AMQP.Connection.Start]

      m1.typeId must ==(m.typeId)
      m1.classId must ==(m.classId)
      m1.methodId must ==(m.methodId)
      m1.majorVersion must ==(m.majorVersion)
      m1.minorVersion must ==(m.minorVersion)
      m1.mechanisms.get must ==(m.mechanisms.get)
      m1.locales.get must ==(m.locales.get)
      
    }
  }
}
