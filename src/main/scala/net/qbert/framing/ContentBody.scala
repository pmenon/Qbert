package net.qbert.framing

import net.qbert.network.{ CanReadIn, FrameReader, FrameWriter }

object ContentBody extends CanReadIn[Option[ContentBody]] {
  def apply(buffer: Array[Byte]) = new ContentBody(buffer)
  def readFrom(fr: FrameReader) = Some(new ContentBody(fr.readBytes(fr.readableBytes-1)))
  def readFrom(fr: FrameReader, size: Int) = Some(new ContentBody(fr.readBytes(size)))
}

class ContentBody(val buffer: Array[Byte]) extends FramePayload {
  val typeId = Frame.FRAME_BODY
  
  def size() = buffer.length

  def writeTo(fw: FrameWriter) = fw.writeBytes(buffer)
}
