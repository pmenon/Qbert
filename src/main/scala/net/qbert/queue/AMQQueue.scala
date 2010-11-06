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
    // we can notify if we have subscribers and one accepts the message
    val canNotify = subscribers.length != 0 && notifySubscribers(m)
    
    // add the entry
    entries.addEntry(m)
  }
  def dequeue(): Option[AMQMessage] = entries.removeEntry.map(_.msg).orElse(None)
  def subscribe(subscription: Subscription): Unit = subscribers += subscription

  def canBeDelivered(msg: AMQMessage, sub: Subscription) = sub.consumer.onEnqueue(msg)

  def notifySubscribers(msg: AMQMessage): Boolean = (subscribers.takeWhile(!canBeDelivered(msg, _)).length) != subscribers.length
}

trait Durable extends AMQQueue {
  val store: Store

  // store this queue
  store.storeQueue(this)

  abstract override def enqueue(m: AMQMessage) = {
    store.storeQueueMessage(m, this)
    super.enqueue(m)
  }

  abstract override def dequeue() = {
    val m = super.dequeue()
    if(m.isDefined) store.removeQueueMessage(m.get, this)
    m
  }
}

trait Exclusive extends AMQQueue {
  val exclusiveOwner: AMQProtocolSession

  abstract override def notifySubscribers(m: AMQMessage) = {
    //exclusiveSubscriber.consumer.onEnqueue(entry)
    super.notifySubscribers(m)
    true
  }
}

trait AutoDelete extends AMQQueue {

}


class SimpleAMQQueue(name: String, virtualHost: AMQVirtualHost) 
  extends AMQQueue(name, virtualHost)

class DurableQueue(name: String, virtualHost: AMQVirtualHost, val store: Store) 
  extends AMQQueue(name, virtualHost) with Durable

class DurableExclusiveQueue(name: String, virtualHost: AMQVirtualHost, store: Store, val exclusiveOwner: AMQProtocolSession)
  extends DurableQueue(name, virtualHost, store) with Exclusive

class DurableExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store, exclusiveOwner: AMQProtocolSession)
  extends DurableExclusiveQueue(name, virtualHost, store, exclusiveOwner) with AutoDelete

class DurableAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store) 
  extends DurableQueue(name, virtualHost, store) with AutoDelete

class ExclusiveQueue(name: String, virtualHost: AMQVirtualHost, val exclusiveOwner: AMQProtocolSession)
  extends AMQQueue(name, virtualHost) with Exclusive

class ExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, exclusiveOwner: AMQProtocolSession)
  extends ExclusiveQueue(name, virtualHost, exclusiveOwner) with AutoDelete

class AutoDeleteQueue(name: String, virtualHost: AMQVirtualHost) 
  extends AMQQueue(name, virtualHost) with AutoDelete
