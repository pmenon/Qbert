package net.qbert.framing

import net.qbert.network.{ CanReadIn, FrameReader}
import net.qbert.protocol.ProtocolVersion

object ContentHeaderFactory {
  def createWithVersion(version: ProtocolVersion) = new ContentHeaderFactory_091
}

trait ContentHeaderFactory {
  def createContentHeaderFrom(fr: FrameReader): Option[ContentHeader]
}

class ContentHeaderFactory_091 extends ContentHeaderFactory {
  def createContentHeaderFrom(fr: FrameReader) = Some(ContentHeader(fr))
}
