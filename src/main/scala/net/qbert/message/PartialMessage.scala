package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader}

import scala.collection.mutable

class ContentChunk(val chunk: Array[Byte])

case class PartialMessage(info: Option[MessagePublishInfo], header: Option[ContentHeader] ) {
  private val chunks = new mutable.ArrayBuffer[ContentBody]()
  
  def sizeReceived() = chunks.foldLeft(0)( (acc, c) => acc + c.size )
  def sizeLeft() = header.map( (h) => h.bodySize - sizeReceived() ).getOrElse(Integer.MAX_VALUE)

  def addContent(chunk: ContentBody) = chunks += chunk

  def body() = ContentBody(chunks.foldLeft(Array[Byte]())( (acc, chunk) => acc ++ chunk.buffer ))
}
