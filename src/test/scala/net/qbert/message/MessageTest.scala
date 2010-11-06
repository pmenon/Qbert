package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader }

import org.specs._

class MessageTest extends Specification {
  "PartialMessages" should {
    "properly assemble chunks" in {
      val string = "Hello, world!".getBytes("utf-8")
      val bodies = string.map((c) => ContentBody(Array[Byte](c)))
      
      val header = ContentHeader(60, 0, string.size, null)
      val message = PartialMessage(None, Some(header))
      
      message.sizeReceived must ==(0)

      bodies.foreach((body) => message.addContent(body))

      message.sizeLeft() must ==(0)
      message.sizeReceived() must ==(string.length)
    }
  }

  "AMQMessages" should {
    "do thangs" in {
    }
  }
}
