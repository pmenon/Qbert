package net.qbert.network

import net.qbert.framing.AMQDataBlock

trait FrameDecoder {
  def decode(fr: FrameReader): AMQDataBlock
}