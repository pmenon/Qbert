package net.qbert.queue

import net.qbert.message.AMQMessage

trait QueueConsumer {
  def onEnqueue(msg: AMQMessage): Boolean
  def onDequeue(msg: AMQMessage): Boolean
}
