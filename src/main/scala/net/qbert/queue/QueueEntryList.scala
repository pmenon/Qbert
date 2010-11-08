package net.qbert.queue

import net.qbert.message.AMQMessageReference
import net.qbert.subscription.Subscription

import scala.collection.mutable

case class QueueEntry(msg: AMQMessageReference) {
  // subscriptions that have rejected this message
  private val rejects = mutable.HashSet[Subscription]()

  // increment reference count since this entry holds onto it
  msg.incrementCount()

}

class QueueEntryList(val queue: AMQQueue) {
  private var entries = mutable.ListBuffer[QueueEntry]()
  private var head: QueueEntryNode = _
  private var tail: QueueEntryNode = head

  class QueueEntryNode(val entry: QueueEntry, var next: Option[QueueEntryNode])

  def addEntry(entry: QueueEntry) = entries += entry
  def removeEntry(entry: QueueEntry) = entries -= entry
  /*
  def addEntry(entry: QueueEntry) = {
    val newNode = new QueueEntryNode(entry, None)
    tail.next = Some(newNode)
    tail = newNode
  }

  def removeEntry(entry: QueueEntry): Option[QueueEntry] = try {
      entries = entries.filter(_ ne entry)
    } catch {
      case e: NoSuchElementException => None
  }
  */

  def compact() = {}
}
