package net.qbert

import java.net.InetSocketAddress
import java.util.concurrent.{ Executors, ExecutorService, TimeUnit }

import net.qbert.network.netty.AMQChannelPipelineFactory

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.{ Channel, ChannelFactory }
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

object Qbert {

  def main(args: Array[String]) = {
    new QbertBroker().start(args)
  }

}

class QbertBroker {
  private var socketFactory: ChannelFactory = _
  private var bossExecutorService: ExecutorService = _
  private var workerExecutorService: ExecutorService = _
  private var serverBootstrap: ServerBootstrap = _
  private var serverChannel: Channel = _

  def start(args: Array[String]) = {
    println("Starting Qbert ...")

    bossExecutorService = Executors.newCachedThreadPool
    workerExecutorService = Executors.newCachedThreadPool
    socketFactory = new NioServerSocketChannelFactory(bossExecutorService, workerExecutorService)
    serverBootstrap = new ServerBootstrap(socketFactory)

    serverBootstrap.setPipelineFactory(new AMQChannelPipelineFactory)
    serverChannel = serverBootstrap.bind(new InetSocketAddress(5672))

    println("Qbert started ...")
  }

  def stop() = {
    println("Stopping Qbert ...")

    serverChannel.disconnect.awaitUninterruptibly
    serverChannel.unbind.awaitUninterruptibly
    serverChannel.close.awaitUninterruptibly

    workerExecutorService.shutdownNow
    workerExecutorService.awaitTermination(3600, TimeUnit.SECONDS)
    bossExecutorService.shutdownNow
    bossExecutorService.awaitTermination(3600, TimeUnit.SECONDS)

    println("Qbert has been stopped ...")
  }
}
