package net.qbert.framing

import net.qbert.network.{ CanWriteTo, CanReadFrom, FrameReader, FrameWriter }

import scala.collection.mutable

class FieldTableDeserializer extends TypeDeserializer {
  def readFieldTable(fr: FrameReader): AMQFieldTable = {
    val res = mutable.Map[AMQShortString, AMQType]()
    val s = fr.readLong
    val tableFR = new FrameReader(fr.readBytes(s))

    while(tableFR.readableBytes > 0) res.put(tableFR.readShortString, readType(tableFR))

    AMQFieldTable(Map[AMQShortString, AMQType]() ++ res)
  }
}

object AMQFieldTable extends CanReadFrom[AMQFieldTable] {
  val deserializer = new FieldTableDeserializer

  def apply():AMQFieldTable = apply(Map[AMQShortString, AMQType]())
  def apply(props: Map[AMQShortString, AMQType]) = new UnencodedFieldTable(props)
  def apply(bytes: Array[Byte]) = new EncodedFieldTable(bytes)
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = deserializer readFieldTable fr
}

trait AMQFieldTable extends CanWriteTo with HasSize {
  val props: Map[AMQShortString, AMQType]
  lazy val tableSize = props.foldLeft(0){ (acc,tuple) => acc + tuple._1.size + tuple._2.size }

  def size() = 4 + tableSize
  def get(key: AMQShortString): Option[AMQType]= props.get(key)  
  def writeTo(fw: FrameWriter) = {
    //val tempWriter = new FrameWriter
    //props foreach{ case (name, value) => name.writeTo(tempWriter); value.writeTo(tempWriter) }
    //val size = tempWriter.bytesWritten
    fw.writeLong(tableSize)
    props foreach{ case (name, value) => name.writeTo(fw); value.writeTo(fw) }
    //map foreach{ case (name, value) => name.writeTo(fw); value.writeTo(fw) }
    //fw.writeFrom(tempWriter)
  }
}

class UnencodedFieldTable(val props: Map[AMQShortString, AMQType]) extends AMQFieldTable

class EncodedFieldTable(val encodedArr: Array[Byte]) extends AMQFieldTable {
  lazy val props = deserialize()

  def deserialize() = {
    val fr = new FrameReader(encodedArr)
    fr.readFieldTable.props
  }
  
  override def writeTo(fw: FrameWriter) = {
    fw.writeBytes(encodedArr)
  }

  override def size() = 4 + encodedArr.length
}
