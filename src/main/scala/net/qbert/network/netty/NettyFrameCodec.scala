package net.qbert.network.netty

import java.net.SocketAddress

import net.qbert.network.FrameReader
import net.qbert.framing.{ AMQFrameCodec, Frame }
import net.qbert.network.FrameDecoder
import net.qbert.protocol.ProtocolVersion
import net.qbert.util.Logging

import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.{ Channels, Channel, ChannelHandlerContext, SimpleChannelUpstreamHandler, MessageEvent }
import org.jboss.netty.handler.codec.frame.{ FrameDecoder => NFrameDecoder }
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

object NettyCodec {
  val initialDecoder = "initialDecoder"
  val frameDecoder = "frameDecoder"
  val frameEncoder = "frameEncoder"

  //def createDecoder() = new NettyInitialAMQDecoder
  def createDecoder() = new QbertDecoder
  //def createDecoder(version: ProtocolVersion) = new NettyAMQFrameDecoder(version)
  def createEncoder() = new QbertEncoder

  implicit def channelToVersionAware(ch: Channel) = new { def getVersion() = ChannelAttributes.getVersion(ch) }
}

/*
trait NettyDecoder {
  val createDecoder: FrameDecoder
  def doDecode(buf: ChannelBuffer): Object = {
    buf.markReaderIndex
    createDecoder.decode(new FrameReader(buf)) match {
      case None =>
        buf.resetReaderIndex
        null
      case Some(frame) =>
        frame
    }
  }
}

/**
 * The initial protocol initiation createDecoder.  This createDecoder delegates to an
 * actual protocol initiation createDecoder implementation to decode AMQP protocol
 * frames.  Once the negotiation process is complete, it removes itself from
 * the pipeline and inserts an AMQP frame createDecoder as per the spec.  Before
 * doing so, it acquires the agreed upon protocol version from the session
 * and uses this to instantiate the new createDecoder in the pipeline.  This is
 * required since the broker is multi-versioned.
 *
 * Note: When the protocol is first agreed upon, the first frame will be handled
 * by a call placed here.  All further frame-decodes will be performed by the
 * NettyFrameDecoder in place in the pipeline
 */
class NettyInitialAMQDecoder extends NFrameDecoder with NettyDecoder {
  val createDecoder = AMQFrameCodec.protocolInitiationDecoder

  override def decode(ctx: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer) = {
    val protocolVersion = ChannelAttributes.getVersion(ch)
    if (protocolVersion ne null) {
      val frameDecoder = NettyCodec.createDecoder(protocolVersion)

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
  val createDecoder = AMQFrameCodec.frameDecoder(version)

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
  var cumulation: ChannelBuffer = ChannelBuffers.EMPTY_BUFFER
  var doDecode: (ChannelHandlerContext, ChannelBuffer, SocketAddress) => Any = initialDecode

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = {
    val buf = e.getMessage.asInstanceOf[ChannelBuffer]
    val remoteAddress = e.getRemoteAddress
    cumulation = cumulateIfNeeded(ctx)
    if(cumulation.readable) {
      cumulation.discardReadBytes
      cumulation.writeBytes(buf)
      doDecode(ctx, cumulation, remoteAddress)
    } else {
      doDecode(ctx, buf, remoteAddress)
      if(buf.readable) cumulation.writeBytes(buf)
    }
  }

  def cumulateIfNeeded(ctx: ChannelHandlerContext) = {
    if(!cumulation.readable) ChannelBuffers.dynamicBuffer(ctx.getChannel.getConfig.getBufferFactory) else cumulation
  }

  def initialDecode(ctx: ChannelHandlerContext, buf: ChannelBuffer, address: SocketAddress) = {
    if(buf.readableBytes >= 4 + 1 + 1 + 1 + 1) {
      Channels.fireMessageReceived(ctx, buf, address)
      doDecode = regularDecode
    }
  }

  def regularDecode(ctx: ChannelHandlerContext, buf: ChannelBuffer, address: SocketAddress) = {
    def canDecode = {
      val availablePayload = (buf.readableBytes) - (1 + 2 + 4 + 1)
      if(availablePayload > 0) {
        val actualLengthOffset = buf.readerIndex + 3
        val size = buf.getUnsignedInt(actualLengthOffset)
        size > 0 && availablePayload >= size
      } else {
        false
      }
    }
    while(canDecode) Channels.fireMessageReceived(ctx, buf, address)
  }
}
