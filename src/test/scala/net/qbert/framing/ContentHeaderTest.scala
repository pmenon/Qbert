package net.qbert.framing

import net.qbert.network.{ FrameReader, FrameWriter }
import org.specs._
import java.util.{ Date => JDate }

class ContentHeaderTest extends Specification {

  "BasicProperties" should {
    "be serializeable" in {

      val b = BasicProperties(Some(AMQShortString("c1")), Some(AMQShortString("ce1")), 
                              Some(AMQFieldTable()), Some(2.asInstanceOf[Byte]),
                              Some(4.asInstanceOf[Byte]),
                              None, Some(AMQShortString("reply")), 
                              Some(AMQShortString("exp")), Some(AMQShortString("message1")), 
                              Some(new JDate(10000)), Some(AMQShortString("type")), 
                              Some(AMQShortString("userId1")), Some(AMQShortString("1")), 
                              None)

      val writer = new FrameWriter(b.size)
      b writeTo writer

      val reader = new FrameReader(writer frame)
      val b1 = BasicProperties(reader)

      b1.contentType must beSome[AMQShortString].which(_.get.equals("c1"))
      b1.contentEncoding must beSome[AMQShortString].which(_.get.equals("ce1"))
      b1.deliveryMode must beSome[Byte].which(_ == 2)
      b1.timestamp must beSomething
      b1.userId must beSome[AMQShortString].which(_.get.equals("userId1"))
      b1.correlationId must beNone
      b1.clusterId must beNone
      b1.messageId must beSome[AMQShortString].which(_.get.equals("message1"))
      b1.typ must beSome[AMQShortString].which(_.get.equals("type"))

      b1.timestamp must beSome[JDate].which(_.equals(new JDate(10000)))

    }
  }

  "ContentHeaders" should {
    "be serializable" in {
      val basicProps = BasicProperties(Some(AMQShortString("c1")), None, 
                            Some(AMQFieldTable()), Some(2.asInstanceOf[Byte]),
                            None,
                            None, Some(AMQShortString("reply")), 
                            None, Some(AMQShortString("message1")), 
                            Some(new JDate(10000)), Some(AMQShortString("type")), 
                            Some(AMQShortString("userId1")), Some(AMQShortString("1")), Some(AMQShortString("cl1")))
      val headers = ContentHeader(60, 14, basicProps)
      val writer = new FrameWriter(headers.size)
      headers writeTo writer

      val reader = new FrameReader(writer frame)
      val headers1 = ContentHeader(reader)

      headers1.classId must ==(60)
      headers1.weight must ==(14)
      val b2 = headers.props

      b2.contentType must beSome[AMQShortString].which(_.get.equals("c1"))
      b2.replyTo must beSome[AMQShortString].which(_.get.equals("reply"))
      b2.priority must beNone
      b2.correlationId must beNone
      //b2.headers must ==(AMQFieldTable())
      b2.timestamp must beSome[JDate].which(_.equals(new JDate(10000)))
    }
  }
  
}

