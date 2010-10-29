package net.qbert.network.netty

import net.qbert.network.{FrameReader, FrameWriter}
import net.qbert.framing.{AMQFrameEncoder, AMQFrameDecoder, AMQFrameDecoderImpl, AMQProtocolInitializationDecoderImpl, Frame}

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{ChannelHandlerContext, Channel}
import org.jboss.netty.handler.codec.frame.FrameDecoder
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

class AMQDecoder extends FrameDecoder {
  private var decoder: AMQFrameDecoder = new AMQProtocolInitializationDecoderImpl
  private var firstDecode = true

  override def decode(c: ChannelHandlerContext, ch: Channel, buf: ChannelBuffer):Object = {
    buf.markReaderIndex
    decoder.decode(new FrameReader(buf)) match {
      case None =>
        buf.resetReaderIndex
        null
      case Some(frame) =>
        if(firstDecode) firstDecode = false; decoder = new AMQFrameDecoderImpl
        frame
    }
  }
}

class AMQEncoder extends OneToOneEncoder {
  private val encoder = new AMQFrameEncoder
  override def encode(c: ChannelHandlerContext, ch: Channel, msg: Object):Object = {
    val message = msg match {
      case frame: Frame => encoder.encode(frame)
      case _ => msg
    }

    message
  }
}
