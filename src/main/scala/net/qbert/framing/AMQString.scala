package net.qbert.framing

import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

object AMQShortString extends CanReadFrom[AMQShortString] {
  def apply(s: String) = new AMQShortString(s)
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    val len = fr.readOctet
    new String( fr.readBytes(len), "utf-8")
  }
  
  implicit def stringToShortSting(s: String):AMQShortString = new AMQShortString(s)
}

class AMQShortString(val s: String) extends CanWriteTo with HasSize {
  val bytes = s.getBytes("utf-8")
  def size() = 2 + bytes.length
  def writeTo(fw: FrameWriter) = {
    fw.writeShort(bytes.length)
    fw.writeBytes(bytes)
  }
  def get() = s
}

object AMQLongString extends CanReadFrom[AMQLongString] {
  def apply(s: String) = new AMQLongString(s.getBytes("utf-8"))
  def apply(bytes: Array[Byte]) = new AMQLongString(bytes)
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    val len = fr.readLong
    new AMQLongString(fr.readBytes(len))
  }
}

class AMQLongString(val longString: Array[Byte]) extends CanWriteTo with HasSize {
  def size() = 4 + longString.length
  def writeTo(fw: FrameWriter) = {
    fw.writeLong(longString.length)
    fw.writeBytes(longString)
  }
}
