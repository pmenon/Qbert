import java.net.InetSocketAddress
import java.util.concurrent.Executors

import net.qbert.network.netty.AMQChannelPipelineFactory

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

object Qbert {
  def main(args: Array[String]) = {
    start(args)
  }

  def start(args: Array[String]) = {
    println("Starting Qbert ...")

    val factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool, 
                                                    Executors.newCachedThreadPool)
    val bootstrap = new ServerBootstrap(factory)
    
    bootstrap.setPipelineFactory(new AMQChannelPipelineFactory)
    bootstrap.bind(new InetSocketAddress(5672))
    
    println("Qbert started ...")
  }

  def stop() = {}
}
