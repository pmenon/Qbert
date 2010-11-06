package net.qbert.virtualhost

import net.qbert.exchange.{ AMQExchange, ExchangeConfiguration, ExchangeTypes }
import net.qbert.queue.{ AMQQueue, QueueConfiguration }

import net.qbert.virtualhost.AMQVirtualHost
import org.specs._
import org.specs.mock.Mockito

class AMQVirtualHostTest extends Specification with Mockito {


  "A Virtual Host" should {
    val virtualHost = AMQVirtualHost("/")

    "be able to create and add a queue" in {

      val queue = virtualHost createQueue QueueConfiguration("test-1", virtualHost, true, true, true)
      queue must beSome[AMQQueue]
      virtualHost.lookupQueue(queue.get.name) must beSome[AMQQueue]

    }

    "be able to create and add an exchange" in {
      val exchange = virtualHost createExchange ExchangeConfiguration("exchange1", ExchangeTypes.DIRECT, true, true, true)
      exchange must beSome[AMQExchange]
      virtualHost.lookupExchange(exchange.get.name) must beSome[AMQExchange]
    }

    "have default exchanges when created" in {
      virtualHost.lookupExchange(ExchangeTypes.DEFAULT) must beSome[AMQExchange].which(_.name.equals(ExchangeTypes.DEFAULT))
      virtualHost.lookupExchange(ExchangeTypes.DEFAULT_DIRECT) must beSome[AMQExchange].which(_.name.equals(ExchangeTypes.DEFAULT_DIRECT))
    }

  }
}
