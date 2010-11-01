package net.qbert.framing

import net.qbert.network.{ CanReadFrom, FrameReader, FrameWriter }

object ContentBody extends CanReadFrom[Option[ContentBody]] {
  def apply(buffer: Array[Byte]) = new ContentBody(buffer)
  def readFrom(fr: FrameReader) = Some(new ContentBody(fr.readBytes(fr.readableBytes)))
}

class ContentBody(buffer: Array[Byte]) extends FramePayload {
  val typeId = Frame.FRAME_BODY
  
  def size() = buffer.length

  def writeTo(fw: FrameWriter) = fw.writeBytes(buffer)

}
