package net.qbert.network.netty

import net.qbert.network.FrameReader
import net.qbert.framing.{ AMQFrameCodec, Frame }
import net.qbert.network.FrameDecoder
import net.qbert.protocol.ProtocolVersion
import net.qbert.util.Logging

import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.{ ChannelHandlerContext, Channel, SimpleChannelUpstreamHandler, MessageEvent }
import org.jboss.netty.handler.codec.frame.{ FrameDecoder => NFrameDecoder }
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

object NettyCodec {
  val initialDecoder = "initialDecoder"
  val frameDecoder = "frameDecoder"
  val frameEncoder = "frameEncoder"

  //def decoder() = new NettyInitialAMQDecoder
  def decoder() = new QbertDecoder
  //def decoder(version: ProtocolVersion) = new NettyAMQFrameDecoder(version)
  def encoder() = new QbertEncoder

  implicit def channelToVersionAware(ch: Channel) = new { def getVersion() = ChannelAttributes.getVersion(ch) }
}

/*
trait NettyDecoder {
  val decoder: FrameDecoder
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
class NettyInitialAMQDecoder extends NFrameDecoder with NettyDecoder {
  val decoder = AMQFrameCodec.protocolInitiationDecoder

  override def decode(ctx: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer) = {
    val protocolVersion = ChannelAttributes.getVersion(ch)
    if (protocolVersion ne null) {
      val frameDecoder = NettyCodec.decoder(protocolVersion)

      ctx.getPipeline().addAfter(NettyCodec.initialDecoder,
                               NettyCodec.frameDecoder, 
                               frameDecoder)
      ctx.getPipeline().remove(this)
      ChannelAttributes.setVersion(ch, null)
      frameDecoder.doDecode(buf)
    } else {
      doDecode(buf)
    }
  }
}

class NettyAMQFrameDecoder(val version: ProtocolVersion) extends NFrameDecoder with NettyDecoder {
  val decoder = AMQFrameCodec.frameDecoder(version)

  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer) = doDecode(buf)
}
*/

class QbertEncoder extends OneToOneEncoder {
  private val encoder = AMQFrameCodec.frameEncoder
  override def encode(c: ChannelHandlerContext, ch: Channel, msg: Object) = {
    val message = msg match {
      case frame: Frame => encoder.encode(frame)
      case _ => ChannelBuffers.EMPTY_BUFFER
    }

    message
  }
}

class QbertDecoder extends SimpleChannelUpstreamHandler with Logging {
  var doDecode: (ChannelHandlerContext, MessageEvent) => Any = initialDecode

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = doDecode(ctx, e)

  def initialDecode(ctx: ChannelHandlerContext, e: MessageEvent) = {
    val buf = e.getMessage.asInstanceOf[ChannelBuffer]
    if(buf.readableBytes < 4 + 1 + 1 + 1 + 1) null
    else {
      ctx.sendUpstream(e)
      doDecode = regularDecode
    }
  }

  def regularDecode(ctx: ChannelHandlerContext, e: MessageEvent) = {
    val buf = e.getMessage.asInstanceOf[ChannelBuffer]
    val availablePayload = (buf.readableBytes) - (1 + 2 + 4 + 1)

    if(availablePayload < 0) {
      null
    } else {
      val actualLengthOffset = buf.readerIndex + 3
      val size = buf.getUnsignedInt(actualLengthOffset)
      if(size > 0 && availablePayload >= size) ctx.sendUpstream(e)
      else null
    }
  }
}
