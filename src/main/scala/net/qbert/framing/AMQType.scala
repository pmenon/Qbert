package net.qbert.framing

import java.util.{ Date => JDate }
import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

class TypeDeserializer {

  def readType(fr: FrameReader): AMQType = {
    fr.readOctet match {
      case 'A' => ArrayType(fr.readArray)
      case 'T' => TimestampType(fr.readTimestamp)
      case 't' => BooleanType(fr.readOctet == 1)
      case 'U' => ShortType(fr.readShort)
      case 'F' => FieldTableType(fr.readFieldTable)
      case 'I' => IntType(fr.readLong)
      case 'l' => LongType(fr.readLongLong)
      case 's' => ShortStringType(fr.readShortString)
      case 'S' => LongStringType(fr.readLongString)
      case _ => println("value type not found"); error("sdf")
    }
  }
}

object AMQType extends CanReadFrom[AMQType] {
  val deserializer = new TypeDeserializer

  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = deserializer readType fr

  implicit def booleanToAMQType(b: Boolean) = BooleanType(b)
  
}

abstract class AMQType extends CanWriteTo with HasSize

case class BooleanType(b: Boolean) extends AMQType {
  def size() = 1 + 1
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('t')
    fw.writeOctet(if(b) 1 else 0)
  }
}
case class ByteType(b: Byte) extends AMQType {
  def size() = 1 + 1
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('b')
    fw.writeOctet(b)
  }
}
case class ShortType(short: Short) extends AMQType {
  def size() = 1 + 2
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('u')
    fw.writeShort(short)
  }
}
case class IntType(i: Int) extends AMQType {
  def size() = 1 + 4
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('I')
    fw.writeLong(i)
  }
}
case class LongType(long: Long) extends AMQType {
  def size() = 1 + 8
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('l')
    fw.writeLongLong(long)
  }
}
case class FloatType(float: Float) extends AMQType {
  def size() = 1 + 4
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('f')
    fw.writeFloat(float)
  }
}
case class ShortStringType(shortString: AMQShortString) extends AMQType {
  def size() = 1 + shortString.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('s')
    fw.writeShortString(shortString)
  }
}
case class LongStringType(longString: AMQLongString) extends AMQType {
  def size() = 1 + longString.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('S')
    fw.writeLongString(longString)
  }
}
case class TimestampType(timestamp: JDate) extends AMQType {
  def size() = 8
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('T')
    fw.writeLongLong(timestamp.getTime()/1000)
  }
}
case class ArrayType(array: AMQArray) extends AMQType {
  def size() = 1 + array.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('A')
    fw.writeArray(array)
  }
}
case class FieldTableType(table: AMQFieldTable) extends AMQType {
  def size() = 1 + table.size
  def writeTo(fw: FrameWriter) = {
    fw.writeOctet('F')
    fw.writeFieldTable(table)
  }
}
