package net.qbert.framing

import net.qbert.network.{ CanWriteOut, FrameWriter }

import java.util.{ Date => JDate}

object AMQType {
  implicit def byteToType(b: Byte) = OctetType(b)
  implicit def shortToType(s: Short) = ShortType(s)
  implicit def intToType(i: Int) = LongType(i)
  implicit def longToType(ll: Long) = LongLongType(ll)
  implicit def timestampToType(t: JDate) = TimestampType(t)

  implicit def optionalByteToType(b: Option[Byte]) = if(b.isDefined) Some(OctetType(b.get)) else None
  implicit def optinonalTimestampToType(t: Option[JDate]) = if(t.isDefined) Some(TimestampType(t.get)) else None

}

abstract class AMQType extends CanWriteOut with HasSize

case class OctetType(value: Byte) extends AMQType {
  def size() = 1
  def writeTo(fw: FrameWriter) = fw.writeOctet(value)
}

case class ShortType(value: Short) extends AMQType {
  def size() = 2
  def writeTo(fw: FrameWriter) = fw.writeShort(value)
}

case class LongType(value: Int) extends AMQType {
  def size() = 4
  def writeTo(fw: FrameWriter) = fw.writeLong(value)
}

case class LongLongType(value: Long) extends AMQType {
  def size() = 8
  def writeTo(fw: FrameWriter) = fw.writeLongLong(value)
}

case class TimestampType(value: JDate) extends AMQType {
  def size() = 8
  def writeTo(fw: FrameWriter) = fw.writeTimestamp(value)
}
