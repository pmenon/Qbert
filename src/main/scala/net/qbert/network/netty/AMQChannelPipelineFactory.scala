package net.qbert.network.netty

import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.Channels

class AMQChannelPipelineFactory extends ChannelPipelineFactory {
  override def getPipeline = {
    val p = Channels.pipeline
   
    // add encoder
    p addLast(NettyCodec.frameEncoder, NettyCodec.encoder)

    p addLast(NettyCodec.initialDecoder, NettyCodec.decoder)
    p addLast("handler", new NettyDelegatingHandler)

    p
  }
}
