package net.qbert.network

import net.qbert.network.netty.{ NettyCodec, QbertDecoder }
import net.qbert.framing.MethodFactory
import net.qbert.protocol.ProtocolVersion

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.{ ChannelHandlerContext, MessageEvent }
import org.jboss.netty.channel.SimpleChannelHandler
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder

import org.specs._
import org.specs.mock.Mockito

class NettyCodecTest extends Specification with Mockito {
  val incompleteMsg = "AMQP".getBytes
  val protocolInitiation = "AMQP0091".getBytes
  val protocolVersion = ProtocolVersion(0, 9)
  val frame = MethodFactory.createWithVersion(protocolVersion).createConnectionOpenOk("test").generateFrame(0)

  "The netty decoder" should {
    "return if there are insufficient bytes in protocol initiation frame" in {
      // create a mock handler
      val mockHandler = mock[SimpleChannelHandler]

      // create a real decoder
      val decoder = NettyCodec.createDecoder

      // create a decoder embedder that simualtes a channelpipeline
      val embedder = new DecoderEmbedder(decoder, mockHandler)

      // create a buffer with not enough data for a protocol initiation frame
      val notEnoughBuffer = ChannelBuffers.wrappedBuffer(incompleteMsg)

      // offer it to the pipeline
      embedder.offer(notEnoughBuffer)

      // ensure the message was not sent upstream
      there was no(mockHandler).handleUpstream(any, any)
    }

    "send protocol initiation frame upstream if fully received" in {
      // create a mock handler
      val mockHandler = mock[SimpleChannelHandler]

      // create a real decoder
      val decoder = NettyCodec.createDecoder

      // create a decoder embedder that simualted a channelpipeline
      val embedder = new DecoderEmbedder(decoder, mockHandler)

      // create a complete protocol initiation frame
      val completeBuffer = ChannelBuffers.wrappedBuffer(protocolInitiation)

      // offer it to the pipeline
      embedder.offer(completeBuffer)

      // ensure the message was sent upstream exactly once
      there was one(mockHandler).handleUpstream(any, any)
    }

    "switch to normal frame decoding after protocol initiation" in {
      // create a mock handler
      val mockHandler = mock[SimpleChannelHandler]

      // create a real decoder
      val decoder = NettyCodec.createDecoder

      // create a decoder embedder that simualted a channelpipeline
      val embedder = new DecoderEmbedder(decoder, mockHandler)

      // create a complete protocol initiation frame
      val completeBuffer = ChannelBuffers.wrappedBuffer(protocolInitiation)

      // create a frame writer to produce a frame
      val writer = new FrameWriter(frame.size)
      frame.writeTo(writer)

      // offer the protocol intiation first
      embedder.offer(completeBuffer)

      // ensure the message was sent upstream exactly once
      there was one(mockHandler).handleUpstream(any, any)

      // offer a full frame
      embedder.offer(writer.frame)

      // ensure the message was sent upstream
      there was two(mockHandler).handleUpstream(any, any)
    }
  }
}