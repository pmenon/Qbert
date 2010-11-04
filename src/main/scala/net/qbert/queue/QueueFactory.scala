package net.qbert.queue

import net.qbert.virtualhost.AMQVirtualHost

case class QueueConfiguration(val name: String, val host: AMQVirtualHost, val durable: Boolean, val exclusive: Boolean, val autoDelete: Boolean)

object QueueFactory {
  def createQueue(queueConfig: QueueConfiguration): AMQQueue = queueConfig match {
    case QueueConfiguration(_, _, false, false, false) => new SimpleAMQQueue(queueConfig.name, queueConfig.host)
    case QueueConfiguration(_, _, true, false, false) => new DurableQueue(queueConfig.name, queueConfig.host, queueConfig.host.store)
    case QueueConfiguration(_, _, true, true, false) => new DurableExclusiveQueue(queueConfig.name, queueConfig.host, queueConfig.host.store, null)
    case QueueConfiguration(_, _, true, true, true) => new DurableExclusiveAutoDeleteQueue(queueConfig.name, queueConfig.host, queueConfig.host.store, null)
    case QueueConfiguration(_, _, false, true, true) => new ExclusiveAutoDeleteQueue(queueConfig.name, queueConfig.host, null)
    case QueueConfiguration(_, _, true, false, true) => new DurableAutoDeleteQueue(queueConfig.name, queueConfig.host, queueConfig.host.store)
    case QueueConfiguration(_, _, false, false, true) => new AutoDeleteQueue(queueConfig.name, queueConfig.host)
    case _ => new SimpleAMQQueue("test", AMQVirtualHost("/"))
  }
}