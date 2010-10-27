package net.qbert.virtualhost

import net.qbert.connection.AMQConnection
import net.qbert.framing.AMQP
import net.qbert.protocol.AMQProtocolSession
import net.qbert.channel.AMQChannel
import net.qbert.exchange.AMQExchange
import net.qbert.queue.SimpleAMQQueue
import net.qbert.virtualhost.AMQVirtualHost

import org.jboss.netty.channel.Channel

import org.specs._

class AMQVirtualHostTest extends Specification {

  /*
  class MockBuffer extends Channel

  class MockConn(m: MockBuffer) extends AMQConnection(m)

  class MockProtocolSession extends AMQProtocolSession {
    val conn = new MockConn(new MockBuffer)
  }
  */

  "A Virtual Host should" should {
    "be able to create and add a queue" in {
      val virtualHost = AMQVirtualHost("/")

      //virtualHost createQueue "test-1"
      /*
      val channel = new AMQChannel(0, new MockProtocolSession)
      val exchange = new AMQExchange("exch1")
      val queue = new SimpleAMQQueue("q1", virtualHost)

      exchange bind(queue, "test-1")

      channel frameReceived AMQP.Basic.Publish("exch1", "test-1")
      channel frameReceived ContentHeader()
      channel frameReceived ContentBody("Simple Body".getBytes("utf-8"))

      val m = queue dequeue
      */
    }
  }
}
