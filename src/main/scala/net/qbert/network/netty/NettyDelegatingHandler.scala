package net.qbert.network.netty

import net.qbert.connection.AMQConnection
import net.qbert.framing.AMQDataBlock
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolDriver

import org.jboss.netty.channel.{ChannelStateEvent, ChannelHandlerContext, SimpleChannelHandler, MessageEvent, ExceptionEvent}

class NettyDelegatingHandler extends SimpleChannelHandler with Logging {
  var protocolDriver: AMQProtocolDriver = null

  final override def channelConnected(c: ChannelHandlerContext, e: ChannelStateEvent) = {
    logDebug("Channel connected ...")
    protocolDriver = new AMQProtocolDriver(new AMQConnection(new NettyChannel(e.getChannel)))
  }

  final override def messageReceived(c: ChannelHandlerContext, m: MessageEvent) = {
    logDebug("Message received ...")
    m getMessage match {
      case dataBlock: AMQDataBlock => protocolDriver dataBlockReceived dataBlock
      case _ => error("Unknown message received ...")
    }
  }

  final override def exceptionCaught(c: ChannelHandlerContext, e: ExceptionEvent) = {
    error("Error occured in channel: " + e)
  }

}
