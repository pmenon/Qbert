package net.qbert.exchange

import net.qbert.queue.AMQQueue
import net.qbert.message.AMQMessage
import net.qbert.store.Store

import scala.collection.mutable

case class Binding(val queue: AMQQueue, val routingKey: String)

abstract class AMQExchange(val name: String) {

  def bind(q: AMQQueue, routingKey: String): Unit
  def route(message: AMQMessage, routingKey: String): List[AMQQueue]
}

trait Durable extends AMQExchange {
  val store: Store

  store.storeExchange(this)
}

class DirectExchange(exchangeName: String) extends AMQExchange(exchangeName) {
  val bindings = mutable.HashMap[String, mutable.HashSet[Binding]]()

  def bind(q: AMQQueue, routingKey: String) = {
    val binding = Binding(q, routingKey)
    bindings.getOrElseUpdate(routingKey, new mutable.HashSet[Binding]) += binding
  }

  def route(message: AMQMessage, routingKey: String) = {
    bindings.get(routingKey).map((set) =>
      set.foldLeft(List[AMQQueue]())( (l,b) => 
        b.queue :: l)
    ).getOrElse(List[AMQQueue]())
  }

}

class DurableDirectExchange(exchangeName: String, val store: Store) extends DirectExchange(exchangeName) with Durable
