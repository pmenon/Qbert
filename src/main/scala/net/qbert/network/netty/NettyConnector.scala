package net.qbert.network.netty

import java.net.InetSocketAddress
import java.util.concurrent.{ Executors, ExecutorService, TimeUnit }

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{ Channel, ChannelFactory, ChannelHandler, ChannelPipelineFactory, StaticChannelPipeline }
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.channel.{ ChannelHandlerContext, ChannelStateEvent, ExceptionEvent, MessageEvent, SimpleChannelHandler }

import net.qbert.broker.QbertComponent
import net.qbert.network.{ Connection, FrameReader, FrameReceiver }
import net.qbert.util.Logging

/**
 * NettyConnector
 *
 * @author: <a href="www.prashanthmenon.com">Prashanth Menon</a>
 *
 * @version: 11-01-02
 */

class NettyConnector(receiver: (Connection) => FrameReceiver) extends QbertComponent with Logging {
  private var channelFactory: ChannelFactory = _
  private var bossExecutorService: ExecutorService = _
  private var workerExecutorService: ExecutorService = _
  private var serverBootstrap: ServerBootstrap = _
  private var serverChannel: Channel = _
  private val channelGroup = new DefaultChannelGroup

  def start() = {
    log.info("Starting the Netty acceptor ...")

    bossExecutorService = Executors.newCachedThreadPool
    workerExecutorService = Executors.newCachedThreadPool
    channelFactory = new NioServerSocketChannelFactory(bossExecutorService, workerExecutorService)
    serverBootstrap = new ServerBootstrap(channelFactory)

    serverBootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline = {
        // encoder, decoder and handler
        val nettyEncoder = NettyCodec.encoder
        val nettyDecoder = NettyCodec.decoder
        val nettyHandler = new NettyDelegatingHandler(receiver)

        val stages = Array[ChannelHandler](nettyEncoder, nettyDecoder, nettyHandler)
        val pipeline = new StaticChannelPipeline( stages: _* )

        pipeline
      }
    })
    serverBootstrap.setOption("child.tcpNoDelay", true)
    serverBootstrap.setOption("child.keepAlive", true)
    serverBootstrap.setOption("child.reuseAddress", true)

    serverChannel = serverBootstrap.bind(new InetSocketAddress(5672))

    log.info("Netty acceptor has been started ...")
    true
  }

  def stop() = {
    log.info("Stopping the Netty Acceptor ...")

    serverChannel.disconnect.awaitUninterruptibly
    serverChannel.unbind.awaitUninterruptibly
    serverChannel.close.awaitUninterruptibly

    workerExecutorService.shutdownNow
    workerExecutorService.awaitTermination(3600, TimeUnit.SECONDS)
    bossExecutorService.shutdownNow
    bossExecutorService.awaitTermination(3600, TimeUnit.SECONDS)

    log.info("The Netty acceptor has been stopped ...")
    true
  }


  class NettyDelegatingHandler(factory: (Connection) => FrameReceiver) extends SimpleChannelHandler with Logging {
    var delegate: FrameReceiver = _

    final override def channelConnected(c: ChannelHandlerContext, e: ChannelStateEvent) = {
      log.debug("Channel connected ...")
      channelGroup.add(e.getChannel)
      delegate = factory(new NettyConnection(e.getChannel))
    }

    final override def messageReceived(c: ChannelHandlerContext, m: MessageEvent) = {
      log.debug("Message received ...")
      val dataBlock = new FrameReader(m.getMessage.asInstanceOf[ChannelBuffer])
      delegate.frameReceived(dataBlock)
    }

    final override def exceptionCaught(c: ChannelHandlerContext, e: ExceptionEvent) = {
      error("Error occured in channel: " + e)
      channelGroup.close.awaitUninterruptibly
    }

  }
}