package net.qbert.queue

import net.qbert.store.Store
import net.qbert.message.AMQMessage
import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost

import scala.collection.mutable
import net.qbert.protocol.AMQProtocolSession

abstract class AMQQueue(val name: String, val virtualHost: AMQVirtualHost) {
  val entries = new QueueEntryList(this)
  val subscribers = mutable.ArrayBuffer[Subscription]()

  def enqueue(m: AMQMessage): Unit = {
    println("Queue " + name + " received message: " + new String(m.body.buffer, "utf-8"))
    entries.addEntry(m)
  }
  def dequeue(): QueueEntry = entries.removeEntry()
  def subscribe(subscription: Subscription): Unit = subscribers += subscription

  def notifySubscribers(entry: QueueEntry) = subscribers.foreach(_.consumer.onEnqueue(entry))
}

trait Durable extends AMQQueue {
  val store: Store

  abstract override def enqueue(m: AMQMessage) = {
    // store.storeEnqueue
    super.enqueue(m)
  }
}

trait Exclusive extends AMQQueue {
  val exclusiveOwner: AMQProtocolSession

  abstract override def notifySubscribers(entry: QueueEntry) = {
    //exclusiveSubscriber.consumer.onEnqueue(entry)
  }
}

trait AutoDelete extends AMQQueue {

}


class SimpleAMQQueue(name: String, virtualHost: AMQVirtualHost) extends AMQQueue(name, virtualHost)

class DurableQueue(name: String, virtualHost: AMQVirtualHost, val store: Store) extends AMQQueue(name, virtualHost) with Durable

class DurableExclusiveQueue(name: String, virtualHost: AMQVirtualHost, store: Store, val exclusiveOwner: AMQProtocolSession)
  extends DurableQueue(name, virtualHost, store)

class DurableExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store, exclusiveOwner: AMQProtocolSession)
  extends DurableExclusiveQueue(name, virtualHost, store, exclusiveOwner)

class DurableAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store) extends DurableQueue(name, virtualHost, store)
  with AutoDelete

class ExclusiveQueue(name: String, virtualHost: AMQVirtualHost, val exclusiveOwner: AMQProtocolSession)
  extends AMQQueue(name, virtualHost) with Exclusive

class ExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, exclusiveOwner: AMQProtocolSession)
  extends ExclusiveQueue(name, virtualHost, exclusiveOwner) with AutoDelete

class AutoDeleteQueue(name: String, virtualHost: AMQVirtualHost) extends AMQQueue(name, virtualHost) with AutoDelete
