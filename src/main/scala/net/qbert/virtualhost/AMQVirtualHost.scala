package net.qbert.virtualhost

import net.qbert.util.Registry
import net.qbert.store.MemoryStore
import net.qbert.queue.{QueueConfiguration, QueueManager, AMQQueue}
import net.qbert.exchange.{ ExchangeConfiguration, ExchangeTypes, ExchangeManager }

object VirtualHostRegistry extends Registry[String, AMQVirtualHost] {
  register("/", new AMQVirtualHost("/"))
}

object AMQVirtualHost {
  def apply(name: String) = new AMQVirtualHost(name)
}

class AMQVirtualHost(val name: String) extends QueueManager with ExchangeManager {
  //val store = new Store{}
  val store = new MemoryStore()

  // default exchanges
  createExchange(ExchangeConfiguration("amq.direct", ExchangeTypes.DIRECT, false, false, false))
  createExchange(ExchangeConfiguration("", ExchangeTypes.DIRECT, false, false, false))

  override def createQueue(queueConfig: QueueConfiguration): Option[AMQQueue] = {
    // check number of queues in virtual host
    super.createQueue(queueConfig)
  }
}
