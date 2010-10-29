package net.qbert.connection

import net.qbert.protocol.ProtocolVersion
import net.qbert.framing.AMQDataBlock

import org.jboss.netty.channel.Channel

class AMQConnection(c: Channel) {
  private var version: ProtocolVersion = null

  def writeFrame(dataBlock: AMQDataBlock) = c.write(dataBlock)

}
