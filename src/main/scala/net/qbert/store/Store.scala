package net.qbert.store

import net.qbert.message.AMQMessage
import net.qbert.queue.AMQQueue
import net.qbert.exchange.AMQExchange

trait Store {

  // messages
  def storeMessage(m: AMQMessage): Unit
  def removeMessage(mId: Int): Unit
  def retrieveMessage(mId: Int): Option[AMQMessage]

  // queues
  def storeQueue(q: AMQQueue): Unit
  def removeQueue(q: AMQQueue): Unit
  def storeQueueMessage(m: AMQMessage, q: AMQQueue): Unit
  def removeQueueMessage(m: AMQMessage, q: AMQQueue): Unit

  // exchanges
  def storeExchange(ex: AMQExchange): Unit
  def removeExchange(ex: AMQExchange): Unit
}
