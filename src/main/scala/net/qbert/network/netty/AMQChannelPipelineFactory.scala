package net.qbert.network.netty

import org.jboss.netty.channel.{ ChannelHandler, ChannelPipelineFactory, StaticChannelPipeline }

/*
class AMQChannelPipelineFactory extends ChannelPipelineFactory {

  override def getPipeline = {
    //val p = Channels.pipeline

    val encoder = NettyCodec.encoder
    val decoder = NettyCodec.decoder
    val handler = new NettyDelegatingHandler
   
    // add encoder
    /*
    p.addLast(NettyCodec.frameEncoder, NettyCodec.encoder)

    p.addLast(NettyCodec.initialDecoder, NettyCodec.decoder)
    //p addLast("executor", new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
    p.addLast("handler", new NettyDelegatingHandler)

    p
    */
    val stages = Array[ChannelHandler](encoder, decoder, handler)
    val pipeline = new StaticChannelPipeline( stages: _* )

    pipeline
  }
}
*/
