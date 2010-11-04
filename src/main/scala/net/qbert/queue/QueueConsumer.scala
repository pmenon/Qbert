package net.qbert.queue

trait QueueConsumer {
  def onEnqueue(entry: QueueEntry): Unit
  def onDequeue(entry: QueueEntry): Unit
}
