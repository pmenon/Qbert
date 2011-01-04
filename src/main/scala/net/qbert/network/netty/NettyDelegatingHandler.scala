package net.qbert.network.netty

import net.qbert.connection.AMQConnection
import net.qbert.framing.AMQDataBlock
import net.qbert.network.FrameHandler
import net.qbert.util.Logging
import net.qbert.protocol.AMQProtocolDriver

import org.jboss.netty.channel.{ChannelStateEvent, ChannelHandlerContext, SimpleChannelHandler, MessageEvent, ExceptionEvent}

/*
class NettyDelegatingHandler(handler: FrameHandler) extends SimpleChannelHandler with Logging {
  var protocolDriver: AMQProtocolDriver = null

  final override def channelConnected(c: ChannelHandlerContext, e: ChannelStateEvent) = {
    log.debug("Channel connected ...")
    protocolDriver = new AMQProtocolDriver(new AMQConnection(new NettyConnection(e.getChannel)))
  }

  final override def messageReceived(c: ChannelHandlerContext, m: MessageEvent) = {
    log.debug("Message received ...")
    m getMessage match {
      case dataBlock: AMQDataBlock => protocolDriver dataBlockReceived dataBlock
      case _ => error("Unknown message received ...")
    }
  }

  final override def exceptionCaught(c: ChannelHandlerContext, e: ExceptionEvent) = {
    error("Error occured in channel: " + e)
  }

}
*/
