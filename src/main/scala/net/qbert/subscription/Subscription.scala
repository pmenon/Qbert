package net.qbert.subscription

import net.qbert.queue.{ AMQQueue, QueueConsumer }

case class Subscription(consumer: QueueConsumer, queue: AMQQueue) {

}


