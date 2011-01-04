package net.qbert.framing

import net.qbert.network.{ CanWriteOut, FrameWriter }

object AMQDataBlock {
  val PROTOCOL_INITIATION = 1
  val REGULAR_FRAME = 2
}

trait AMQDataBlock extends CanWriteOut {
  val frameType: Int

  def size(): Int
}

trait FramePayload extends CanWriteOut {
  val typeId: Int

  def size(): Int

  def generateFrame(channelId: Int): Frame = {
    Frame(typeId, channelId, this)
  }
}

object Frame {
  val FRAME_METHOD = 1
  val FRAME_CONTENT = 2
  val FRAME_BODY = 3
  val FRAME_HEARTBEAT = 4
  val FRAME_DELIMETER = 0xCE.asInstanceOf[Byte]

  def apply(typeId: Int, channelId: Int, payload: FramePayload): Frame = {
    new Frame(typeId, channelId, payload.size, payload)
  }

  def createMethodFrame(channelId: Int, payload: FramePayload): Frame = apply(payload.typeId, channelId, payload)

}

class Frame(val typeId: Int, val channelId: Int, val length: Int, val payload: FramePayload) extends AMQDataBlock {
  val frameType = AMQDataBlock.REGULAR_FRAME

  def size() = {
    1 + 2 + 4 + payload.size() + 1
  }

  def writeTo(fw: FrameWriter) = {
    fw.writeOctet(typeId)
    fw.writeShort(channelId)
    fw.writeLong(length)
    payload.writeTo(fw)
    fw.writeOctet(Frame.FRAME_DELIMETER)
  }

  override def toString() = "#Frame<typeId="+typeId+"channel="+channelId+">\n --->"+payload
}

object ProtocolInitiation {
  val AMQP_HEADER = "AMQP".asInstanceOf[Array[Byte]]

  def apply(header: Array[Byte], classId: Byte, instance: Byte, major: Byte, minor: Byte) = {
    new ProtocolInitiation(header, classId, instance, major, minor)
  }
}

class ProtocolInitiation(val header: Array[Byte], val classId: Byte, val major: Byte, val minor: Byte, val revision: Byte) extends AMQDataBlock {
  val frameType = AMQDataBlock.PROTOCOL_INITIATION

  def size() = 4 + 1 + 1 + 1 + 1

  def writeTo(fw: FrameWriter) = {
    fw.writeBytes(header)
    fw.writeOctet(classId)
    fw.writeOctet(major)
    fw.writeOctet(minor)
    fw.writeOctet(revision)
  }

  override def toString() = "#ProtocolInitialization<classId="+classId+",major="+major+",minor="+minor+",revision="+revision+">"

}

