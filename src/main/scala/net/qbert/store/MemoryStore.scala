package net.qbert.store

import net.qbert.message.AMQMessage
import net.qbert.exchange.AMQExchange
import net.qbert.queue.AMQQueue

import scala.collection.mutable

class MemoryStore extends Store {
  private val messages = new mutable.HashMap[Int, AMQMessage]()
  private val queues = new mutable.HashMap[String, AMQQueue]()
  private val queueMessages = new mutable.HashMap[String, mutable.HashSet[Int]]()
  private val exchanges = new mutable.HashMap[String, AMQExchange]()

  def storeMessage(m: AMQMessage) = messages.put(m.id, m)
  def removeMessage(messageId: Int) = messages.remove(messageId)
  def retrieveMessage(messageId: Int) = messages.get(messageId)

  def storeQueue(q: AMQQueue) = queues.put(q.name, q)
  def retrieveQueue(q: AMQQueue) = queues.get(q.name)
  def retrieveQueueMessages(q: AMQQueue) = queueMessages.get(q.name).map((messageIds) =>
    messageIds.foldLeft(List[AMQMessage]())( (acc,mId)=> 
      messages.get(mId).map(_ :: acc).getOrElse(acc)
    )).getOrElse(List[AMQMessage]())
  def removeQueue(q: AMQQueue) = queues.remove(q.name)
  def storeQueueMessage(m: AMQMessage, q: AMQQueue) = queueMessages.getOrElseUpdate(q.name, new mutable.HashSet[Int]()) += m.id
  def removeQueueMessage(m: AMQMessage, q: AMQQueue) = queueMessages.get(q.name).map(_ -= m.id)

  def storeExchange(ex: AMQExchange) = exchanges.put(ex.name, ex)
  def removeExchange(ex: AMQExchange) = exchanges.remove(ex.name)

}
