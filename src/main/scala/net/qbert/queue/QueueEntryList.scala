package net.qbert.queue


import scala.collection.mutable

class QueueEntryList(queue: AMQQueue) {
  val entries = mutable.Queue[QueueEntry]()

  def addEntry(entry: QueueEntry) = entries.enqueue(entry)

  def removeEntry() = entries.dequeue

  def compact() = {}
}