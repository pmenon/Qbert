package net.qbert.virtualhost

import net.qbert.exchange.{ AMQExchange, ExchangeRegistry }
import net.qbert.queue.{ AMQQueue, QueueRegistry }
import net.qbert.util.Registry

import scala.collection.mutable

object VirtualHostRegistry extends Registry[String, AMQVirtualHost]

object AMQVirtualHost {
  def apply(name: String) = new AMQVirtualHost(name)
}

class AMQVirtualHost(val name: String) {
  val queueRegistry = new QueueRegistry
  val exchangeRegistry = new ExchangeRegistry

}
