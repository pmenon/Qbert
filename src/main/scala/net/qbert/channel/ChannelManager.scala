package net.qbert.channel

import net.qbert.connection.AMQConnection
import net.qbert.protocol.AMQProtocolSession
import net.qbert.util.Registry

class ChannelRegistry extends Registry[Long, AMQChannel]

trait ChannelManager { self: AMQConnection =>
  val channelMap = new ChannelRegistry
  val maxChannels: Int

  def channel(channelId: Int) = getChannel(channelId)

  def getChannel(channelId: Int) = channelMap.get(channelId)

  def createChannel(channelId: Int): AMQChannel = {
    val channel = new AMQChannel(channelId, this)
    registerChannel(channel)
    channel
  }

  def registerChannel(channel: AMQChannel) = channelMap.register(channel.channelId, channel)
}
