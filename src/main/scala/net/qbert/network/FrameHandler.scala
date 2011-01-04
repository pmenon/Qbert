package net.qbert.network

import net.qbert.framing.AMQDataBlock

trait FrameHandler extends FrameReceiver {
  val decoder: FrameDecoder

  def frameReceived(fr: FrameReader) = handleFrame(decoder.decode(fr))

  def handleFrame(frame: AMQDataBlock)
}