package net.qbert.network

import net.qbert.framing.{ AMQArray, AMQShortString, AMQLongString, FieldTable }
import java.util.{Date => JDate}
import scala.collection.mutable
import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }

trait CanReadFrom[A]{
  def readFrom(fr: FrameReader): A
}

class FrameReader(buf: ChannelBuffer) {
  def this(byteArray: Array[Byte]) = this(ChannelBuffers.wrappedBuffer(byteArray))

  def readBytes(b: Array[Byte]): Unit = buf.readBytes(b)
  def readBytes(len: Int): Array[Byte] = {
    val buf = Array.ofDim[Byte](len)
    readBytes(buf)
    buf
  }

  def readOctet(): Byte = buf.readUnsignedByte.asInstanceOf[Byte]
  def readShort(): Short = buf.readUnsignedShort.asInstanceOf[Short]
  def readLong(): Int = buf.readInt
  def readLongLong(): Long = buf.readLong
  def readFloat(): Float = buf.readFloat
  def readTimestamp(): JDate = new JDate(buf.readLong)

  def readArray(): AMQArray = {
    AMQArray(this)
  }

  def readFieldTable(): FieldTable = {
    FieldTable(this)
  }
  /*
  def readArray(): Array[Any] = {
    val len = readInt
    val arr = mutable.ArrayBuffer[Any](len)

    1 to len foreach( i => arr += readFieldValue)
    
    Array[Any]() ++ arr
  }

  def readFieldValue(): Any = {
    readOctet match {
      case 'A' => readArray
      case 'T' => readTimestamp
      case 't' => readOctet
      case 'U' => readShort
      case 'F' => readFieldTable
      case 'I' => readLong
      case 'l' => readLongLong
      case 's' => readShortString
      case 'S' => readLongString
      case _ => println("value type not found"); readOctet
    }
  }

  def readFieldTable(): Map[String, Any] = {
    val res = mutable.Map[String, Any]()
    val s = readLong
    val tableFR = new FrameReader(buf.readBytes(s))

    while(tableFR.readableBytes > 0) res.put(tableFR.readShortString, tableFR.readFieldValue)

    Map[String, Any]() ++ res
  }
  


  def readShortString(): String = {
    val len = readOctet
    val s = new String( readBytes(len), "utf-8" )
    s
  }

  def readLongString(): String = {
    val len = readLong
    val m = new String( readBytes(len), "utf-8" )
    m
  }
  */

  def readShortString(): AMQShortString = AMQShortString(this)

  def readLongString(): AMQLongString = AMQLongString(this)

  def readableBytes = buf.readableBytes
}
