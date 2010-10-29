package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader}

import scala.collection.mutable

class ContentChunk(val chunk: Array[Byte])

class PartialMessage(val info: MessagePublishInfo) {
  private var header: ContentHeader = null
  private val chunks = new mutable.ArrayBuffer[ContentChunk]()

  def setHeader(c: ContentHeader) = header = c

  def addContent(chunk: ContentChunk) = chunks += chunk
}
