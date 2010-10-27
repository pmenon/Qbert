package net.qbert.framing

import net.qbert.network.{ CanWriteTo, FrameWriter }

class ContentHeader(val contentHeader: Option[String], val contentEncoding: Option[String],
                    val headers: Option[Map[String, Any]], val deliveryMode: Option[Int], 
                    val priority: Option[Int], val correlation: Option[String], 
                    val replyTo: Option[String], val expiration: Option[String],
                    val messageId: Option[String], val timestamp: Option[Long], 
                    val headerType: Option[String], val userId: Option[String]) extends FramePayload {
  val typeId = Frame.FRAME_CONTENT

  def size() = 2 + 2 + 8 + 10

  def writeTo(fr: FrameWriter) = {}

  //def contentType
}

