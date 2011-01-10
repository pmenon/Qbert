package net.qbert.network.netty

import org.jboss.netty.channel.{ ChannelHandler, ChannelPipelineFactory, StaticChannelPipeline }

/*
class AMQChannelPipelineFactory extends ChannelPipelineFactory {

  override def getPipeline = {
    //val p = Channels.pipeline

    val createEncoder = NettyCodec.createEncoder
    val createDecoder = NettyCodec.createDecoder
    val handler = new NettyDelegatingHandler
   
    // add createEncoder
    /*
    p.addLast(NettyCodec.frameEncoder, NettyCodec.createEncoder)

    p.addLast(NettyCodec.initialDecoder, NettyCodec.createDecoder)
    //p addLast("executor", new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
    p.addLast("handler", new NettyDelegatingHandler)

    p
    */
    val stages = Array[ChannelHandler](createEncoder, createDecoder, handler)
    val pipeline = new StaticChannelPipeline( stages: _* )

    pipeline
  }
}
*/
