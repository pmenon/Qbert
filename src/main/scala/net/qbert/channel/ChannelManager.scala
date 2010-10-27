package net.qbert.channel

import net.qbert.protocol.AMQProtocolSession
import net.qbert.util.Registry

import java.util.concurrent.atomic
import scala.collection.mutable

class ChannelRegistry extends Registry[Long, AMQChannel]

trait ChannelManager {self: AMQProtocolSession =>
  //val channelMap = new mutable.HashMap[Long, AMQChannel]()
  private val channelMap = new ChannelRegistry

  def channel(channelId: Int) = getChannel(channelId)

  def getChannel(channelId: Int): Option[AMQChannel] = channelMap.get(channelId)

  def createChannel(channelId: Int): AMQChannel = {
    val channel = new AMQChannel(channelId, this)
    registerChannel(channel)
    channel
  }

  def registerChannel(channel: AMQChannel) = channelMap.register(channel.channelId, channel)
}
