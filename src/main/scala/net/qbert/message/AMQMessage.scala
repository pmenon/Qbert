package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader }

case class AMQMessage(id: Int, info: MessagePublishInfo, header: ContentHeader, body: ContentBody)
