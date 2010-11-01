package net.qbert.framing

import java.util.{ Date => JDate }
import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

class FieldValueDeserializer {

  def readFieldValue(fr: FrameReader): AMQFieldValue = {
    fr.readOctet match {
      case 'A' => ArrayFieldValue(fr.readArray)
      case 'T' => TimestampFieldValue(fr.readTimestamp)
      case 't' => BooleanFieldValue(fr.readOctet == 1)
      case 'U' => ShortFieldValue(fr.readShort)
      case 'F' => FieldTableFieldValue(fr.readFieldTable)
      case 'I' => IntFieldValue(fr.readLong)
      case 'l' => LongFieldValue(fr.readLongLong)
      case 's' => ShortStringFieldValue(fr.readShortString)
      case 'S' => LongStringFieldValue(fr.readLongString)
      case _ => println("value type not found"); error("sdf")
    }
  }
}

object AMQFieldValue extends CanReadFrom[AMQFieldValue] {
  val deserializer = new FieldValueDeserializer

  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = deserializer readFieldValue fr
  
}

abstract class AMQFieldValue extends CanWriteTo with HasSize

case class BooleanFieldValue(value: Boolean) extends AMQFieldValue {
  def size() = 1 + 1
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('t')
    fw.writeOctet(if(value) 1 else 0)
  }
}
case class ByteFieldValue(value: Byte) extends AMQFieldValue {
  def size() = 1 + 1
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('b')
    fw.writeOctet(value)
  }
}
case class ShortFieldValue(value: Short) extends AMQFieldValue {
  def size() = 1 + 2
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('u')
    fw.writeShort(value)
  }
}
case class IntFieldValue(value: Int) extends AMQFieldValue {
  def size() = 1 + 4
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('I')
    fw.writeLong(value)
  }
}
case class LongFieldValue(value: Long) extends AMQFieldValue {
  def size() = 1 + 8
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('l')
    fw.writeLongLong(value)
  }
}
case class FloatFieldValue(value: Float) extends AMQFieldValue {
  def size() = 1 + 4
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('f')
    fw.writeFloat(value)
  }
}
case class ShortStringFieldValue(value: AMQShortString) extends AMQFieldValue {
  def size() = 1 + value.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('s')
    fw.writeShortString(value)
  }
}
case class LongStringFieldValue(value: AMQLongString) extends AMQFieldValue {
  def size() = 1 + value.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('S')
    fw.writeLongString(value)
  }
}
case class TimestampFieldValue(value: JDate) extends AMQFieldValue {
  def size() = 8
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('T')
    fw.writeLongLong(value.getTime()/1000)
  }
}
case class ArrayFieldValue(value: AMQArray) extends AMQFieldValue {
  def size() = 1 + value.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('A')
    fw.writeArray(value)
  }
}
case class FieldTableFieldValue(value: AMQFieldTable) extends AMQFieldValue {
  def size() = 1 + value.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('F')
    fw.writeFieldTable(value)
  }
}
