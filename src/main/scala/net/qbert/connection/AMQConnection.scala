package net.qbert.connection

import net.qbert.protocol.ProtocolVersion
import net.qbert.framing.AMQDataBlock
import net.qbert.network.netty.NettyChannel

class AMQConnection(val channel: NettyChannel) {
  private var version: ProtocolVersion = null

  def initialize(pv: ProtocolVersion) = {
    version = pv
    channel.setVersion(pv)
  }

  def writeFrame(dataBlock: AMQDataBlock) = channel.write(dataBlock)

}
