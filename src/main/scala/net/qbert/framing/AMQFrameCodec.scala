package net.qbert.framing

import net.qbert.util.Logging
import net.qbert.network.{ FrameDecoder, FrameReader, FrameWriter }
import net.qbert.protocol.ProtocolVersion

object AMQFrameCodec {
  def protocolInitiationDecoder() = new AMQProtocolInitiationDecoderImpl
  def frameDecoder(version: ProtocolVersion) = new AMQFrameDecoderImpl(version)
  def frameEncoder() = new AMQFrameEncoderImpl
  def decoder() = new StatefulAMQPDecoder
}

/**
 * An AMQP Frame createEncoder needs to accept a frame, encode it the w
 * wire-level format and return the result
 */
trait AMQFrameEncoder {
  def encode(frame: Frame): AnyRef
}

class AMQFrameEncoderImpl extends AMQFrameEncoder {
  def encode(frame: Frame) = {
    val writer = new FrameWriter(frame.size)    
    frame.writeTo(writer)
    writer.frame
  }
}

/**
 * An AMQP Frame Decoder accepts a frame reader and optionally
 * returns an amqp data block.  The data block can either be
 * a protocol initiation frame or a regular AMQP frame.
 */
trait AMQFrameDecoder {
  def decode(fr: FrameReader): Option[AMQDataBlock]
}

/*
class AMQFrameDecoderImpl(version: ProtocolVersion) extends AMQFrameDecoder with Logging {
  val methodFactory = MethodFactory.createWithVersion(version)
  val contentHeaderFactory = ContentHeaderFactory.createWithVersion(version)
  val contentBodyFactory = ContentBody.readFrom(_:FrameReader, _:Int)

  def decode(fr: FrameReader): Option[Frame] = {
    val availablePayload = (fr readableBytes) - (1 + 2 + 4 + 1)
    if(availablePayload < 0) return None

    val typeId = fr readOctet
    val channel = fr readShort
    val size = fr readLong

    if(availablePayload < size) return None

    val payload = if(typeId == Frame.FRAME_METHOD) methodFactory.createMethodFrom(fr)
                  else if (typeId == Frame.FRAME_CONTENT) contentHeaderFactory.createContentHeaderFrom(fr)
                  else if (typeId == Frame.FRAME_BODY) contentBodyFactory(fr, size)
                  else None

    val frame = payload.map(Frame(typeId, channel, _)).orElse(None)

    val frameDelimiter = fr readOctet
    //if(frameDelimiter == Frame.FRAME_DELIMETER) {
    //  println("we good")
    //}

    frame
  }
}

class AMQProtocolInitiationDecoderImpl extends AMQFrameDecoder {
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
*/

class AMQFrameDecoderImpl(version: ProtocolVersion) extends FrameDecoder with Logging {
  val methodFactory = MethodFactory.createWithVersion(version)
  val contentHeaderFactory = ContentHeaderFactory.createWithVersion(version)
  val contentBodyFactory = ContentBody.readFrom(_:FrameReader, _:Int)

  def decode(fr: FrameReader): Frame = {

    val typeId = fr readOctet
    val channel = fr readShort
    val size = fr readLong

    val payload = if(typeId == Frame.FRAME_METHOD) methodFactory.createMethodFrom(fr)
                  else if (typeId == Frame.FRAME_CONTENT) contentHeaderFactory.createContentHeaderFrom(fr)
                  else if (typeId == Frame.FRAME_BODY) contentBodyFactory(fr, size)
                  else None

    val frame = payload.map(Frame(typeId, channel, _)).orElse(None)

    val frameDelimiter = fr readOctet
    //if(frameDelimiter == Frame.FRAME_DELIMETER) {
    //  println("we good")
    //}

    frame.get
  }
}

class AMQProtocolInitiationDecoderImpl extends FrameDecoder {
  def decode(fr: FrameReader): ProtocolInitiation = {

    val header = Array.ofDim[Byte](4)
    fr.readBytes(header)
    val classId = fr.readOctet
    val instance = fr.readOctet
    val major = fr.readOctet
    val minor = fr.readOctet

    new ProtocolInitiation(header, classId, instance, major, minor)
  }
}

class StatefulAMQPDecoder extends FrameDecoder {
  var version: ProtocolVersion = _
  var decoder: FrameDecoder = new AMQProtocolInitiationDecoderImpl

  def decode(fr: FrameReader) = decoder.decode(fr)

  def setVersion(protocolVersion: ProtocolVersion) = {
    version = protocolVersion
    decoder = new AMQFrameDecoderImpl(version)
  }
}