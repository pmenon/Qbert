package net.qbert.exchange

import net.qbert.queue.AMQQueue
import net.qbert.message.AMQMessage

case class Binding(val queue: AMQQueue, val routingKey: String)

abstract class AMQExchange(val exchangeName: String) {
  def bind(q: AMQQueue, routingKey: String): Unit
  def route(message: AMQMessage, routingKey: String): List[AMQQueue]
}

trait Durable extends AMQExchange {

}
