package net.qbert.framing

import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

import scala.collection.mutable

class ArrayDeserializer extends TypeDeserializer {
  def readArray(fr: FrameReader): AMQArray = {
    val len = fr.readLong
    val arr = mutable.ArrayBuffer[AMQType]()

    1 to len foreach( i => arr += readType(fr) )

    AMQArray(Array[AMQType]() ++ arr)
  }
}

object AMQArray extends CanReadFrom[AMQArray] {
  val deserializer = new ArrayDeserializer

  def apply(array: Array[AMQType]) = new AMQArray(array)
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = deserializer readArray fr
}

class AMQArray(array: Array[AMQType]) extends CanWriteTo with HasSize {
  def size() = array.foldLeft(0)( (acc, elem) => acc + elem.size )
  def writeTo(fw: FrameWriter) = {
    fw.writeLong(size())
    array.foreach( elem => elem.writeTo(fw) )
  }
}
