package net.qbert.network

import net.qbert.framing.{ AMQArray, AMQShortString, AMQLongString, AMQFieldTable }
import java.util.{Date => JDate}
import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }

trait CanReadIn[A]{
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
  def readTimestamp(): JDate = new JDate(buf.readLong*1000)

  def readArray(): AMQArray = {
    AMQArray(this)
  }

  def readFieldTable(): AMQFieldTable = {
    AMQFieldTable(this)
  }

  def readShortString(): AMQShortString = AMQShortString(this)

  def readLongString(): AMQLongString = AMQLongString(this)

  def readableBytes = buf.readableBytes
}
