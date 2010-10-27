package net.qbert.subscription

import net.qbert.queue.{ AMQQueue, QueueListener }

case class Subscription(listener: QueueListener, queue: AMQQueue)
