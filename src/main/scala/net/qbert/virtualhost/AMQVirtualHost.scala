package net.qbert.virtualhost

import net.qbert.exchange.{ AMQExchange, ExchangeRegistry }
import net.qbert.util.Registry

import net.qbert.store.Store
import net.qbert.queue.{QueueConfiguration, QueueManager, AMQQueue}

object VirtualHostRegistry extends Registry[String, AMQVirtualHost] {
  register("/", new AMQVirtualHost("/"))
}

object AMQVirtualHost {
  def apply(name: String) = new AMQVirtualHost(name)
}

class AMQVirtualHost(val name: String) extends QueueManager {
  val exchangeRegistry = new ExchangeRegistry
  val store = new Store{}

  override def createQueue(queueConfig: QueueConfiguration): Option[AMQQueue] = {
    // check number of queues in virtual host
    super.createQueue(queueConfig)
  }
}
