package net.qbert.connection

import net.qbert.framing.AMQDataBlock

import org.jboss.netty.channel.Channel

class AMQConnection(c: Channel) {
  def writeFrame(dataBlock: AMQDataBlock) = c.write(dataBlock)
}
