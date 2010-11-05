package net.qbert.queue

import net.qbert.logging.Logging
import net.qbert.util.Registry

class QueueRegistry extends Registry[String, AMQQueue] with Logging {
  def register(queue: AMQQueue): Unit = register(queue.name, queue)
}

trait QueueManager {
  val queueRegistry = new QueueRegistry

  def createQueue(queueConfig: QueueConfiguration): Option[AMQQueue] = {
    val queue = QueueFactory.createQueue(queueConfig)
    queueRegistry.register(queue)
    Some(queue)
  }

  def lookupQueue(name: String) = queueRegistry.get(name)

}
                                           
