package net.qbert.network

import net.qbert.framing.AMQDataBlock

trait Connection {
  def writeFrame(data: AMQDataBlock): Unit

  def close: Unit
}