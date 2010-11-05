package net.qbert.exchange

object ExchangeTypes {
  val DIRECT = "direct"
  val TOPIC = "topic"
  val HEADERS = "headers"
  val FANOUT = "fanout"
}

case class ExchangeConfiguration(name: String, eType: String, durable: Boolean, autoDelete: Boolean, internal: Boolean)

object ExchangeFactory {
  def createExchange(config: ExchangeConfiguration): AMQExchange = config match {
    //case ExchangeConfiguration(_,ExchangeTypes.DIRECT,false,false,false) => new D
    case _ => new DirectExchange(config.name)
  }
}
