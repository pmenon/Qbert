package net.qbert.network

import net.qbert.network.netty.{ NettyCodec, QbertDecoder }
import net.qbert.framing.MethodFactory
import net.qbert.protocol.ProtocolVersion

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.{ ChannelHandlerContext, MessageEvent }

import org.specs._
import org.specs.mock.Mockito

class NettyCodecTest extends Specification with Mockito {
  val incompleteMsg = "AMQP".getBytes
  val protocolInitiation = "AMQP0091".getBytes
  val protocolVersion = ProtocolVersion(0, 9)
  val frame = MethodFactory.createWithVersion(protocolVersion).createConnectionOpenOk("test").generateFrame(0)

  "The netty decoder should" should {
    "return if there are insufficient bytes in protocol initiation frame" in {
      val notEnoughBuffer = ChannelBuffers.wrappedBuffer(incompleteMsg)

      val mockContext = mock[ChannelHandlerContext]
      val mockEvent = mock[MessageEvent]
      mockEvent.getMessage returns (notEnoughBuffer)

      // get a fresh new decoder
      val decoder = NettyCodec.decoder

      // fire a new protocol initiation message
      decoder.messageReceived(mockContext, mockEvent)

      // because the message was incomplete, it should not be sent upstream
      there was no(mockContext).sendUpstream(any)
    }

    "send protocol initiation frame upstream if fully received" in {
      val completeBuffer = ChannelBuffers.wrappedBuffer(protocolInitiation)

      val mockContext = mock[ChannelHandlerContext]
      val mockEvent = mock[MessageEvent]
      mockEvent.getMessage returns (completeBuffer)

      // get a fresh new decoder
      val decoder = NettyCodec.decoder

      // fire a new protocol initiation message
      decoder.messageReceived(mockContext, mockEvent)

      // the message should be sent upstream, the decoder should switch as well
      there was one(mockContext).sendUpstream(any)
    }

    "switch to normal frame decoding after protocol initiation" in {
      val completeBuffer = ChannelBuffers.wrappedBuffer(protocolInitiation)

      val mockContext = mock[ChannelHandlerContext]
      val mockEvent = mock[MessageEvent]
      mockEvent.getMessage returns (completeBuffer)

      // get a fresh new decoder
      val decoder = NettyCodec.decoder

      // fire a new protocol initiation message
      decoder.messageReceived(mockContext, mockEvent)

      // the message should be sent upstream, the decoder should switch as well
      there was one(mockContext).sendUpstream(any)

      // create a frame writer to produce a frame
      val writer = new FrameWriter(frame.size)
      frame.writeTo(writer)
      mockEvent.getMessage returns(writer.frame)

      // fire a full message
      decoder.messageReceived(mockContext, mockEvent)

      // it should be sent upstream
      there was two(mockContext).sendUpstream(any)
    }
  }
}