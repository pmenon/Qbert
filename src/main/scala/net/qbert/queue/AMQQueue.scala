package net.qbert.queue

import net.qbert.store.Store
import net.qbert.message.{AMQMessage, AMQMessageReference}
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolSession
import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost
import net.qbert.util.ActorDelegate

import akka.actor.Actor
import akka.dispatch.Future

import scala.collection.mutable

trait AMQQueue {
  val name: String
  val virtualHost: AMQVirtualHost

  def enqueue(message: AMQMessage): Unit
  def enqueueAndWait(message: AMQMessage): Future[QueueMessages.DeliveryResponse]
  def dequeue(entry: QueueEntry): Unit
  def addSubscription(subscription: Subscription): Unit
  def removeSubscription(subscription: Subscription): Unit
  def stop: Unit
}

sealed abstract class QueueMessage
object QueueMessages {
  case class EnqueueMessage(message: AMQMessageReference) extends QueueMessage
  case class EnqueueWithResponse(message: AMQMessageReference) extends QueueMessage
  case class DequeueMessage(entry: QueueEntry) extends QueueMessage
  case class SubscribeMessage(subscription: Subscription) extends QueueMessage
  case class UnsubscribeMessage(subscription: Subscription) extends QueueMessage
  case class DeliveryResponse(delivered: Boolean) extends QueueMessage
  case object StopQueue extends QueueMessage
}

trait ActorBasedQueue extends ActorDelegate with AMQQueue {
  import QueueMessages._

  private val queueActor = Actor.actorOf(new AMQQueueActor).start

  def enqueue(message: AMQMessage) = invokeNoResult(queueActor, EnqueueMessage(message.reference))
  def enqueueAndWait(message: AMQMessage) = invoke(queueActor, EnqueueWithResponse(message.reference))
  def dequeue(entry: QueueEntry) = invokeNoResult(queueActor, DequeueMessage(entry))
  def addSubscription(sub: Subscription) = invokeNoResult(queueActor, SubscribeMessage(sub))
  def removeSubscription(sub: Subscription) = invokeNoResult(queueActor, UnsubscribeMessage(sub))
  def stop = {
    invokeNoResult(queueActor, StopQueue)
    queueActor.stop
  }

  private class AMQQueueActor extends Actor {

    def receive = {
      case EnqueueMessage(message) => handleEnqueue(message)
      case EnqueueWithResponse(message) =>
        val canNotify = handleEnqueue(message)
        if(message.m.isImmediate()) self.reply(DeliveryResponse(canNotify))
      case DequeueMessage(entry) => handleDequeue(entry)
      case SubscribeMessage(sub) => handleSubscribe(sub)
      case UnsubscribeMessage(sub) => handleUnsubscribe(sub)
      case StopQueue => handleStopQueue
      case _ => log.error("Received unknown message on queue ...")
    }

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
    logInfo("Queue {} received a message: {}", name, new String(m.m.body.buffer, "utf-8"))
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

  def canBeDelivered(entry: QueueEntry, sub: Subscription) = sub.consumer.onEnqueue(sub.consumerTag, entry)
  def notifySubscribers(entry: QueueEntry) = (subscribers.takeWhile(!canBeDelivered(entry, _)).length) != subscribers.length

  def handleStopQueue() = {
    logInfo("Queue {} is stopping ...", name)
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
  abstract override def removeSubscription(sub: Subscription) = {
    super.removeSubscription(sub)
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
