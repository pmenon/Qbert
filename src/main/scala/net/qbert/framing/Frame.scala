package net.qbert.framing

import net.qbert.network.{CanWriteTo, FrameWriter}

trait AMQDataBlock extends CanWriteTo {
  def size(): Int
}

trait FramePayload extends CanWriteTo {
  val typeId: Int

  def size(): Int
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

  def createMethodFrame(channelId: Int, payload: FramePayload): Frame = {
    val f = new Frame(channelId, payload)
    f
  }
}

class Frame(val typeId: Int, val channelId: Int, val length: Int, val payload: FramePayload) extends AMQDataBlock {
  def this(channelId: Int, payload: FramePayload) = this(payload.typeId, channelId, payload.size, payload)

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

  def apply(header: Array[Byte], classId: Int, instance: Int, major: Int, minor: Int) = {
    new ProtocolInitiation(header, classId, instance, major, minor)
  }
}

class ProtocolInitiation(val header: Array[Byte], val classId: Int, val instance: Int, val major: Int, val minor: Int) extends AMQDataBlock {

  def size() = 4 + 1 + 1 + 1 + 1

  def writeTo(fw: FrameWriter) = {
    fw.writeBytes(header)
    fw.writeOctet(classId)
    fw.writeOctet(instance)
    fw.writeOctet(major)
    fw.writeOctet(minor)
  }

  override def toString() = "#ProtocolInitialization<header='"+header(0)+"','"+header(1)+"','"+header(2)+"','"+header(3)+",>"

}

