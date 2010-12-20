package net.qbert.subscription

import net.qbert.queue.{ AMQQueue, QueueConsumer }

case class Subscription(consumerTag: String, consumer: QueueConsumer, queue: AMQQueue) {

}


