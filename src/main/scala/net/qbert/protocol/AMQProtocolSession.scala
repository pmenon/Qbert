package net.qbert.protocol

import net.qbert.channel.ChannelManager
import net.qbert.connection.AMQConnection
import net.qbert.framing.{ Frame, MethodFactory }
import net.qbert.virtualhost.AMQVirtualHost

trait AMQProtocolSession extends ChannelManager {
  val conn: AMQConnection
  var protocolVersion: ProtocolVersion = null
  var virtualHost: Option[AMQVirtualHost] = None
  var methodFactory: MethodFactory = null

  def virtualHost_=(host: AMQVirtualHost) = Some(host)

  def writeFrame(frame: Frame) = conn writeFrame frame

  def init(pv: ProtocolVersion) = {
    conn.initialize(pv)
    protocolVersion = pv
    methodFactory = MethodFactory.createWithVersion(protocolVersion)
  }
}
