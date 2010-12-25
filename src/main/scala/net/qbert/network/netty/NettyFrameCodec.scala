package net.qbert.network.netty

import net.qbert.network.{FrameReader}
import net.qbert.framing.{AMQFrameCodec, AMQFrameDecoder, Frame}
import net.qbert.protocol.ProtocolVersion

import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.{ChannelHandlerContext, Channel }
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

/**
 * The initial protocol initiation decoder.  This decoder delegates to an
 * actual protocol initiation decoder implementation to decode AMQP protocol 
 * frames.  Once the negotiation process is complete, it removes itself from
 * the pipeline and inserts an AMQP frame decoder as per the spec.  Before
 * doing so, it acquires the agreed upon protocol version from the session
 * and uses this to instantiate the new decoder in the pipeline.  This is
 * required since the broker is multi-versioned.
 *
 * Note: When the protocol is first agreed upon, the first frame will be handled
 * by a call placed here.  All further frame-decodes will be performed by the
 * NettyFrameDecoder in place in the pipeline
 */
class NettyInitialAMQDecoder extends FrameDecoder with NettyDecoder {
  val decoder = AMQFrameCodec.protocolInitiationDecoder

  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer) = {
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

  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer) = doDecode(buf)
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
