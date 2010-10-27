package net.qbert.queue

import net.qbert.logging.Logging
import net.qbert.util.Registry

class QueueRegistry extends Registry[String, AMQQueue] with Logging {
  def register(queue: AMQQueue): Unit = register(queue.name, queue)
}
                                           
