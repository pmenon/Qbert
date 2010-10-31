package net.qbert.network.netty

import net.qbert.network.{FrameReader}
import net.qbert.framing.{AMQFrameCodec, AMQFrameDecoder, AMQFrameEncoder, Frame}
import net.qbert.protocol.ProtocolVersion

import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.{ChannelHandlerContext, Channel, ChannelLocal }
import org.jboss.netty.handler.codec.frame.FrameDecoder
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

object NettyCodec {
  val initialDecoder = "initialDecoder"
  val frameDecoder = "frameDecoder"
  val frameEncoder = "frameEncoder"

  def decoder() = new NettyInitialAMQDecoder
  def decoder(version: ProtocolVersion) = new NettyAMQFrameDecoder(version)
  def encoder() = new NettyAMQEncoder

  implicit def channelToVersionAware(ch: Channel) = new { def getVersion() = ChannelAttributes.getVersion(ch) }
}

trait NettyDecoder {
  val decoder: AMQFrameDecoder
  def doDecode(buf: ChannelBuffer): Object = {
    buf.markReaderIndex
    decoder.decode(new FrameReader(buf)) match {
      case None =>
        buf.resetReaderIndex
        null
      case Some(frame) =>
        frame
    }
  }
}

class NettyInitialAMQDecoder extends FrameDecoder with NettyDecoder {
  val decoder = AMQFrameCodec.protocolInitiationDecoder

  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer):Object = {
    import NettyCodec._
    val protocolVersion = ChannelAttributes.getVersion(ch)
    if (protocolVersion ne null) {
      val frameDecoder = NettyCodec.decoder(protocolVersion)
      
      c.getPipeline().addAfter(NettyCodec.initialDecoder, 
                               NettyCodec.frameDecoder, 
                               frameDecoder)
      c.getPipeline().remove(this)
      ChannelAttributes.setVersion(ch, null)
      frameDecoder.doDecode(buf)
    } else {
      doDecode(buf)
    }
  }
}

class NettyAMQFrameDecoder(val version: ProtocolVersion) extends FrameDecoder with NettyDecoder {
  val decoder = AMQFrameCodec.frameDecoder(version)
  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer): Object = doDecode(buf)
}

class NettyAMQEncoder extends OneToOneEncoder {
  private val encoder = AMQFrameCodec.frameEncoder
  override def encode(c: ChannelHandlerContext, ch: Channel, msg: Object):Object = {
    val message = msg match {
      case frame: Frame => encoder.encode(frame)
      case _ => ChannelBuffers.EMPTY_BUFFER
    }

    message
  }
}
