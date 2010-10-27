package net.qbert.queue

//import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost

case class QueueEntry(msg: String)

abstract class AMQQueue(val name: String, val virtualHost: AMQVirtualHost) {
  def enqueue(entry: QueueEntry): Unit
  def dequeue(): Unit
  //def subscribe(subscription: Subscription): Unit
}

//trait P


class SimpleAMQQueue(name: String, virtualHost: AMQVirtualHost) extends AMQQueue(name, virtualHost) {
  def enqueue(entry: QueueEntry) = {}
  def dequeue() = {}
}
