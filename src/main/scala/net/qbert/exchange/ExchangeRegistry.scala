package net.qbert.exchange

import net.qbert.logging.Logging
import net.qbert.util.Registry

class ExchangeRegistry extends Registry[String, AMQExchange] with Logging {
  def register(exchange: AMQExchange): Unit = register(exchange.exchangeName, exchange)
}
