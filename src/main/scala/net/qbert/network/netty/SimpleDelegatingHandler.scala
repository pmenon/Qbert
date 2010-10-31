package net.qbert.network.netty

import net.qbert.connection.AMQConnection
import net.qbert.framing.AMQDataBlock
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolDriver

import org.jboss.netty.channel.{ChannelStateEvent, ChannelHandlerContext, SimpleChannelHandler, MessageEvent, ExceptionEvent}

class SimpleDelegatingHandler extends SimpleChannelHandler with Logging {
  var stateHandler: AMQProtocolDriver = null

  final override def channelConnected(c: ChannelHandlerContext, e: ChannelStateEvent) = {
    debug("Channel connected ...")
    stateHandler = new AMQProtocolDriver(new AMQConnection(new NettyChannel(e.getChannel)))
  }

  final override def messageReceived(c: ChannelHandlerContext, m: MessageEvent) = {
    debug("Message received ...")
    m getMessage match {
      case dataBlock: AMQDataBlock => stateHandler dataBlockReceived dataBlock
      case _ => error("Unknown message received ...")
    }
  }

  final override def exceptionCaught(c: ChannelHandlerContext, e: ExceptionEvent) = {
    error("Error occured in channel: " + e)
  }

}
