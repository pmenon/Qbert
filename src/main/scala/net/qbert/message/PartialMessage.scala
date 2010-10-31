package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader}

import scala.collection.mutable

class ContentChunk(val chunk: Array[Byte])

case class PartialMessage(info: Option[MessagePublishInfo], header: Option[ContentHeader] ) {
  private val chunks = new mutable.ArrayBuffer[ContentChunk]()

  def addContent(chunk: ContentChunk) = chunks += chunk
}
