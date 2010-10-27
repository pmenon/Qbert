package net.qbert.framing

import net.qbert.logging.Logging
import net.qbert.framing.{Frame, MethodFactory_091}
import net.qbert.network.{FrameReader, FrameWriter}

class AMQFrameEncoder {
  def encode(frame: Frame) = {
    val writer = new FrameWriter(frame.size)    
    frame.writeTo(writer)
    writer.frame
  }
}

trait AMQFrameDecoder {
  def decode(fr: FrameReader): Option[AMQDataBlock]
}

class AMQFrameDecoderImpl extends AMQFrameDecoder with Logging {
  val methodFactory = new MethodFactory_091
  def decode(fr: FrameReader): Option[Frame] = {
    val availablePayload = (fr readableBytes) - (1 + 2 + 4 + 1)
    if(availablePayload < 0) return None

    val typeId = fr readOctet
    val channel = fr readShort
    val size = fr readLong

    if(availablePayload < size) return None

    val m = methodFactory createMethodFrom fr
    val frame = m match {
      case Some(method) => Some(Frame(typeId, channel, method))
      case None => None
    }

    val frameDelimiter = fr readOctet
    //if(frameDelimiter == Frame.FRAME_DELIMETER) {
    //  println("we good")
    //}

    frame
  }
}

class AMQProtocolInitializationDecoderImpl extends AMQFrameDecoder {
  def decode(fr: FrameReader): Option[ProtocolInitiation] = {
    if(fr.readableBytes < 4 + 1 + 1 + 1 + 1) return None

    val header = Array.ofDim[Byte](4)
    fr.readBytes(header)
    val classId = fr.readOctet
    val instance = fr.readOctet
    val major = fr.readOctet
    val minor = fr.readOctet

    Some(new ProtocolInitiation(header, classId, instance, major, minor))
  }
}










