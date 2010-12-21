package net.qbert.queue

trait QueueConsumer {
  def onEnqueue(consumerTag: String, entry: QueueEntry): Boolean
  def onDequeue(entry: QueueEntry): Boolean
}
