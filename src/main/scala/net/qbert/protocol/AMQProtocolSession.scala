package net.qbert.protocol

import net.qbert.channel.ChannelManager
import net.qbert.connection.AMQConnection
import net.qbert.framing.{ ContentBody, ContentHeader, Frame, Method, MethodFactory }
import net.qbert.virtualhost.AMQVirtualHost

trait AMQProtocolSession extends ChannelManager {
  val conn: AMQConnection
  var protocolVersion: ProtocolVersion = null
  var virtualHost: Option[AMQVirtualHost] = None
  var methodFactory: MethodFactory = null
  var maxFrameSize = None

  def virtualHost_=(host: AMQVirtualHost) = Some(host)
  def maxFrameSize_=(frameSize: Int) = Some(frameSize)

  def writeFrame(frame: Frame) = conn writeFrame frame

  def init(pv: ProtocolVersion) = {
    conn.initialize(pv)
    protocolVersion = pv
    methodFactory = MethodFactory.createWithVersion(protocolVersion)
  }

  def methodReceived(channelId: Int, method: Method): Unit
  def contentHeaderReceived(channelId: Int, header: ContentHeader): Unit
  def contentBodyReceived(channelId: Int, body: ContentBody): Unit
}
