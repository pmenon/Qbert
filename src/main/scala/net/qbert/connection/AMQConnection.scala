package net.qbert.connection

import net.qbert.protocol.ProtocolVersion
import net.qbert.framing.AMQDataBlock
import net.qbert.network.netty.NettyChannel

sealed abstract class ConnectionState 
case object AwaitingConnectionStartOk extends ConnectionState
case object AwaitingConnectionTuneOk extends ConnectionState
case object AwaitingConnectionOpen extends ConnectionState
case object Opened extends ConnectionState
case object Closing extends ConnectionState
case object Stopped extends ConnectionState

class AMQConnection(val channel: NettyChannel) {
  private var version: ProtocolVersion = null

  def initialize(pv: ProtocolVersion) = {
    version = pv
    channel.setVersion(pv)
  }

  def writeFrame(dataBlock: AMQDataBlock) = channel.write(dataBlock)

}
