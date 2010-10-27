package net.qbert.network

import net.qbert.framing.{AMQP, AMQDataBlock, AMQFrameDecoderImpl, Frame, Method}
import org.specs._

class FrameReaderWriterTest extends Specification {
  "FrameReader should" should {
    "properly parse things" in {
      /*
      val m = new AMQP.Connection.Start(0,9,Map("test"->1, "f"->"hi", "complex"->Map("uh"->"huh")), "simple", "en_US")
      val frame = m.generateFrame(0)
      val fw = new FrameWriter(frame.size)

      frame writeTo fw

      val fr = new FrameReader(fw.frame)
      val decoder = new AMQFrameDecoderImpl

      val frame2 = decoder.decode(fr)

      frame2 must beSome[Frame]
      val m1 = frame2.get.payload.asInstanceOf[Method]

      m1.typeId must_== m.typeId
      m1.classId must_== m.classId
      m1.methodId must_== m.methodId

      val m2 = m1.asInstanceOf[AMQP.Connection.Start]

      m must_== m2
      */
    }
  }
}
