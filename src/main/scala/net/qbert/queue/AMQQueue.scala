package net.qbert.queue

import net.qbert.store.Store
import net.qbert.message.{AMQMessage, AMQMessageReference}
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolSession
import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost

import scala.actors.Future
import scala.actors.Actor
import scala.collection.mutable

trait AMQQueue {
  val name: String
  val virtualHost: AMQVirtualHost

  def enqueue(message: AMQMessage): Unit
  def enqueueAndWait(message: AMQMessage): Future[QueueMessages.DeliveryResponse]
  def dequeue(entry: QueueEntry): Unit
  def subscribe(subscription: Subscription): Unit
  def unsubscribe(subscription: Subscription): Unit
  def close: Unit
}

sealed abstract class QueueMessage
object QueueMessages {
  case class EnqueueMessage(message: AMQMessageReference) extends QueueMessage
  case class DequeueMessage(entry: QueueEntry) extends QueueMessage
  case class SubscribeMessage(subscription: Subscription) extends QueueMessage
  case class UnsubscribeMessage(subscription: Subscription) extends QueueMessage
  case class DeliveryResponse(delivered: Boolean) extends QueueMessage
  case object StopQueue extends QueueMessage
}

trait ActorBasedQueue extends Actor with AMQQueue {
  import QueueMessages._

  start

  def enqueue(message: AMQMessage) = this ! EnqueueMessage(message.reference)
  def enqueueAndWait(message: AMQMessage) = (this !! EnqueueMessage(message.reference)).asInstanceOf[Future[DeliveryResponse]]
  def dequeue(entry: QueueEntry) = this ! DequeueMessage(entry)
  def subscribe(sub: Subscription) = this ! SubscribeMessage(sub)
  def unsubscribe(sub: Subscription) = this ! UnsubscribeMessage(sub)
  def close = this ! StopQueue

  def act() = loop(mainLoop)

  def mainLoop() = react {
    case EnqueueMessage(message) =>
      val canNotify = handleEnqueue(message)
      if(message.m.isImmediate()) reply(DeliveryResponse(canNotify))
    case DequeueMessage(entry) => handleDequeue(entry)
    case SubscribeMessage(sub) => handleSubscribe(sub)
    case UnsubscribeMessage(sub) => handleUnsubscribe(sub)
    case StopQueue => handleStopQueue
  }
  
  protected def handleEnqueue(message: AMQMessageReference): Boolean
  protected def handleDequeue(entry: QueueEntry): Unit
  protected def handleSubscribe(sub: Subscription): Unit
  protected def handleUnsubscribe(sub: Subscription): Unit
  protected def notifySubscribers(entry: QueueEntry): Boolean
  protected def handleStopQueue: Unit
} 

abstract class BaseQueue(val name: String, val virtualHost: AMQVirtualHost) extends ActorBasedQueue with Logging {

  val entries = new QueueEntryList(this)
  val subscribers = mutable.ArrayBuffer[Subscription]()

  def handleEnqueue(m: AMQMessageReference): Boolean = {
    info("Queue {} received a message: {}", name, new String(m.m.body.buffer, "utf-8"))
    val entry = QueueEntry(m)
    // we can notify if we have subscribers and one accepts the message
    val canNotify = subscribers.length != 0 && notifySubscribers(entry)
    
    // add the entry
    if(!(m.m.isImmediate() && !canNotify)) entries.addEntry(entry)

    canNotify
  }
  def handleDequeue(entry: QueueEntry) = entries.removeEntry(entry)
  def handleSubscribe(subscription: Subscription): Unit = subscribers += subscription
  def handleUnsubscribe(subscription: Subscription): Unit = subscribers -= subscription

  def canBeDelivered(entry: QueueEntry, sub: Subscription) = sub.consumer.onEnqueue(entry)
  def notifySubscribers(entry: QueueEntry) = (subscribers.takeWhile(!canBeDelivered(entry, _)).length) != subscribers.length

  def handleStopQueue() = {
    info("Queue {} is stopping ...", name)
    exit()
  }
}

trait Durable extends BaseQueue {
  val store: Store

  // store this queue
  store.storeQueue(this)

  abstract override def handleEnqueue(m: AMQMessageReference) = {
    if(m.m.isPersistent) store.storeQueueMessage(m.m, this)
    super.handleEnqueue(m)
  }

  abstract override def handleDequeue(entry: QueueEntry) = {
    super.dequeue(entry)
    if(entry.msg.m.isPersistent()) store.removeQueueMessage(entry.msg.m, this)
  }
}

trait Exclusive extends BaseQueue {
  val exclusiveOwner: AMQProtocolSession

  abstract override def notifySubscribers(entry: QueueEntry) = {
    //exclusiveSubscriber.consumer.onEnqueue(entry)
    super.notifySubscribers(entry)
    true
  }
}

trait AutoDelete extends BaseQueue {
  abstract override def unsubscribe(sub: Subscription) = {
    super.unsubscribe(sub)
    if(subscribers.length <= 0) delete()
  }

  def delete() = {}
}


class SimpleAMQQueue(name: String, virtualHost: AMQVirtualHost) 
  extends BaseQueue(name, virtualHost)

class DurableQueue(name: String, virtualHost: AMQVirtualHost, val store: Store) 
  extends BaseQueue(name, virtualHost) with Durable

class DurableExclusiveQueue(name: String, virtualHost: AMQVirtualHost, store: Store, val exclusiveOwner: AMQProtocolSession)
  extends DurableQueue(name, virtualHost, store) with Exclusive

class DurableExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store, exclusiveOwner: AMQProtocolSession)
  extends DurableExclusiveQueue(name, virtualHost, store, exclusiveOwner) with AutoDelete

class DurableAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, store: Store) 
  extends DurableQueue(name, virtualHost, store) with AutoDelete

class ExclusiveQueue(name: String, virtualHost: AMQVirtualHost, val exclusiveOwner: AMQProtocolSession)
  extends BaseQueue(name, virtualHost) with Exclusive

class ExclusiveAutoDeleteQueue(name: String, virtualHost: AMQVirtualHost, exclusiveOwner: AMQProtocolSession)
  extends ExclusiveQueue(name, virtualHost, exclusiveOwner) with AutoDelete

class AutoDeleteQueue(name: String, virtualHost: AMQVirtualHost) 
  extends BaseQueue(name, virtualHost) with AutoDelete
