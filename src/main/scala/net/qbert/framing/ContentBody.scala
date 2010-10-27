package net.qbert.framing

import net.qbert.network.FrameWriter

class ContentBody(buffer: Array[Byte]) extends FramePayload {
  val typeId = Frame.FRAME_BODY
  
  def size() = buffer.length

  def writeTo(fw: FrameWriter) = fw.writeBytes(buffer)

}
