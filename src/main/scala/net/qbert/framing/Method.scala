package net.qbert.framing

import net.qbert.framing.Frame
import net.qbert.handler.{ MethodError, MethodHandler }
import net.qbert.network.{FrameWriter, FrameReader}

trait Method extends FramePayload {
  val typeId = Frame.FRAME_METHOD

  val classId: Int 
  val methodId: Int

  def argSize(): Int
  def writeArguments(fw: FrameWriter): Unit
  def handle(channelId: Int, methodHandler: MethodHandler): Either[MethodError, Option[Frame]]

  def writeTo(fw: FrameWriter) = {
    fw.writeShort(classId)
    fw.writeShort(methodId)
    writeArguments(fw)
  }

  def size() = 2 + 2 + argSize

  def generateFrame(channelId: Int): Frame = {
    Frame(typeId, channelId, this)
  }
}







