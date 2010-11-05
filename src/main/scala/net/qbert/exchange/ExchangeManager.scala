package net.qbert.exchange

import net.qbert.logging.Logging
import net.qbert.util.Registry

class ExchangeRegistry extends Registry[String, AMQExchange] with Logging {
  def register(exchange: AMQExchange): Unit = register(exchange.name, exchange)
}

trait ExchangeManager {
  val exchangeRegistry = new ExchangeRegistry

  def createExchange(exchangeConfig: ExchangeConfiguration): Option[AMQExchange] = {
    val ex = ExchangeFactory.createExchange(exchangeConfig)
    exchangeRegistry.register(ex)
    Some(ex)
  }

  def lookupExchange(name: String) = exchangeRegistry.get(name)
}
