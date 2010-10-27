package net.qbert.exchange

import net.qbert.queue.AMQQueue

class AMQExchange(val exchangeName: String) {
  def bind(q: AMQQueue, routingKey: String) = {}
}
