package net.qbert.queue

trait QueueConsumer {
  def onEnqueue(entry: QueueEntry): Boolean
  def onDequeue(entry: QueueEntry): Boolean
}
