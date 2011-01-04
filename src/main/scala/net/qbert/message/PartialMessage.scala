package net.qbert.message

import net.qbert.framing.{ ContentBody, ContentHeader }

import scala.collection.mutable


case class PartialMessage(info: Option[MessagePublishInfo], header: Option[ContentHeader] ) {
  private val chunks = new mutable.ArrayBuffer[ContentBody]()
  private var received = 0

  def sizeReceived() = received
  def sizeLeft() = header.map( (h) => h.bodySize - received ).getOrElse(Integer.MAX_VALUE)

  def addContent(chunk: ContentBody) = {
    received += chunk.size()
    chunks += chunk
  }

  def body() = ContentBody(chunks.foldLeft(Array[Byte]())( (acc, chunk) => acc ++ chunk.buffer ))
}
