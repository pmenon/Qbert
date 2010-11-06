package net.qbert.queue

import net.qbert.message.AMQMessage

import scala.collection.mutable

case class QueueEntry(msg: AMQMessage)

class QueueEntryList(queue: AMQQueue) {
  val entries = mutable.Queue[QueueEntry]()

  def addEntry(m: AMQMessage) = entries.enqueue(QueueEntry(m))

  def removeEntry(): Option[QueueEntry] = try {
      Some(entries.dequeue)
    } catch {
      case e: NoSuchElementException => None
  }

  def compact() = {}
}
