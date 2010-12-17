package net.qbert.framing

import net.qbert.handler.{ MethodHandler, MethodHandlerResponse }
import net.qbert.network.FrameWriter

trait Method extends FramePayload {
  val typeId = Frame.FRAME_METHOD

  val classId: Int 
  val methodId: Int

  def argSize(): Int
  def writeArguments(fw: FrameWriter): Unit
  def handle(channelId: Int, methodHandler: MethodHandler): MethodHandlerResponse

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







