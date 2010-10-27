package net.qbert.network.netty

import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.Channels

class AMQChannelPipelineFactory extends ChannelPipelineFactory {
  override def getPipeline = {
    val p = Channels.pipeline
   
    // add encoder
    p addLast("encoder", new AMQEncoder)

    p addLast("decoder", new AMQDecoder)
    p addLast("handler", new SimpleDelegatingHandler)

    p
  }
}
