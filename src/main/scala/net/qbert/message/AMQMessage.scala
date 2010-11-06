package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader }

class AMQMessage(val id: Int, val info: MessagePublishInfo, val header: ContentHeader, val body: ContentBody) {

  def isMandatory = info.mandatory
  def isImmediate() = info.immediate
  def isPersistent() = header.props.deliveryMode.map(_ != 0).getOrElse(false)

}
