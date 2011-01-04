package net.qbert.channel.connection

import net.qbert.connection.AMQConnection
import net.qbert.network.netty.NettyChannel

import org.specs._
import org.specs.mock.Mockito

class ConnectionTest extends Specification with Mockito {

  "An AMQP Connection should" should {
    "correctly walk through negotiation stages" in {
      //val mockChannel = mock[NettyChannel]

      //val conn = new AMQConnection(mockChannel)
    }
  }
}