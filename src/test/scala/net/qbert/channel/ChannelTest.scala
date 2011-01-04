package net.qbert.channel

import net.qbert.connection.AMQConnection
import net.qbert.framing.amqp_091.{ AMQP_091 => AMQP }

import org.specs._
import org.specs.mock.Mockito

class ChannelTest extends Specification with Mockito {

  "An AMQP channel" should {
    val mockConnection = mock[AMQConnection]
    val channel = new AMQChannel(1, mockConnection)
    /*
    "handle Basic.Publish frames" in {

      channel publishReceived AMQP.Basic.Publish(1.asInstanceOf[Short], "exch1", "test-1", 1.asInstanceOf[Byte])
      channel contentHeaderReceived ContentHeader()
      channel contentBodyReceived ContentBody("Simple Body".getBytes("utf-8"))

      val m = queue dequeue
      
    }
    */
    channel.stop
  }

}
