package net.qbert.framing

import net.qbert.network.{ CanReadIn, FrameReader, FrameWriter }

import scala.collection.mutable

class FieldTableDeserializer extends FieldValueDeserializer {
  def readFieldTable(fr: FrameReader): AMQFieldTable = {
    val res = mutable.Map[AMQShortString, AMQFieldValue]()
    val s = fr.readLong
    val tableFR = new FrameReader(fr.readBytes(s))

    while(tableFR.readableBytes > 0) res.put(tableFR.readShortString, readFieldValue(tableFR))

    AMQFieldTable(Map[AMQShortString, AMQFieldValue]() ++ res)
  }
}

object AMQFieldTable extends CanReadIn[AMQFieldTable] {
  val deserializer = new FieldTableDeserializer

  def apply():AMQFieldTable = apply(Map[AMQShortString, AMQFieldValue]())
  def apply(props: Map[AMQShortString, AMQFieldValue]) = new UnencodedFieldTable(props)
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    val s = fr.readLong
    new EncodedFieldTable(s, fr.readBytes(s))
  }
}

trait AMQFieldTable extends AMQType {
  val props: Map[AMQShortString, AMQFieldValue]
  lazy val tableSize = props.foldLeft(0){ (acc,tuple) => acc + tuple._1.size + tuple._2.size }

  def size() = 4 + tableSize
  def get(key: AMQShortString) = props.get(key)
  def writeTo(fw: FrameWriter) = {
    fw.writeLong(tableSize)
    props foreach{ case (name, value) => name.writeTo(fw); value.writeTo(fw) }
  }
}

class UnencodedFieldTable(val props: Map[AMQShortString, AMQFieldValue]) extends AMQFieldTable

class EncodedFieldTable(val eSize: Int, val encodedArr: Array[Byte]) extends AMQFieldTable {
  lazy val props = deserialize()

  def deserialize() = {
    val fr = new FrameReader(Array(eSize).asInstanceOf[Array[Byte]] ++ encodedArr)
    AMQFieldTable.deserializer.readFieldTable(fr).props
  }
  
  override def writeTo(fw: FrameWriter) = {
    fw.writeLong(eSize)
    fw.writeBytes(encodedArr)
  }

  override def size() = 4 + encodedArr.length
}
