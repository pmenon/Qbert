package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader }

class AMQMessage(val id: Int, val info: MessagePublishInfo, val header: ContentHeader, val body: ContentBody) {
  val reference = new AMQMessageReference(this)

  def isMandatory = info.mandatory
  def isImmediate() = info.immediate
  def isPersistent() = header.props.deliveryMode.map(_ == 2).getOrElse(false)  

}

class AMQMessageReference(val m: AMQMessage) {
  private var refCount = 0
  def incrementCount() = synchronized {
    refCount += 1
  }
  def decrementCout() = synchronized {
    refCount -= 1
  }
}
