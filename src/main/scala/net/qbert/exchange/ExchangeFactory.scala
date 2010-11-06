package net.qbert.exchange

object ExchangeTypes {
  val DEFAULT = ""
  val DIRECT = "direct"
  val DEFAULT_DIRECT = "amq.direct"
  val TOPIC = "topic"
  val DEFAULT_TOPIC = "amq.topic"
  val HEADERS = "headers"
  val DEFAULT_HEADERS = "amq.headers"
  val FANOUT = "fanout"
  val DEFAULT_FANOUT = "amq.fanout"
}


case class ExchangeConfiguration(name: String, eType: String, durable: Boolean, autoDelete: Boolean, internal: Boolean)

object ExchangeFactory {
  def createExchange(config: ExchangeConfiguration): AMQExchange = config match {
    //case ExchangeConfiguration(_,ExchangeTypes.DIRECT,false,false,false) => new D
    case _ => new DirectExchange(config.name)
  }
}
