package net.qbert.message

case class MessagePublishInfo(exchangeName: String, routingKey: String, mandatory: Boolean, immediate: Boolean)
