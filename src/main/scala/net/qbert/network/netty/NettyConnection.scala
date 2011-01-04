package net.qbert.network.netty

import net.qbert.protocol.ProtocolVersion
import org.jboss.netty.channel.{ Channel, ChannelLocal }
import net.qbert.network.Connection
import net.qbert.framing.AMQDataBlock

object ChannelAttributes {
  private val channelLocal = new ChannelLocal[ProtocolVersion]
  def getVersion(ch: Channel) = channelLocal.get(ch)
  def setVersion(ch: Channel, pv: ProtocolVersion) = channelLocal.set(ch, pv)
}

class NettyChannel(private var ch: Channel) {
  def setVersion(pv: ProtocolVersion) = ChannelAttributes.setVersion(ch, pv)
  def getVersion() = ChannelAttributes.getVersion(ch)
  def write(db: Any) = ch.write(db)
}

class NettyConnection(val ch: Channel) extends Connection {
  def writeFrame(db: AMQDataBlock) = ch.write(db)
  def close = ch.close
}
