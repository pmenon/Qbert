package net.qbert.broker

import net.qbert.network.netty.NettyConnector
import net.qbert.network.Connection
import net.qbert.protocol.AMQProtocolDriver
import net.qbert.util.Logging


object QbertBroker {

  def main(args: Array[String]) = {
    new QbertBroker().start(args)
  }

}

class QbertBroker extends Logging {
  var connector = new NettyConnector((conn: Connection) => new AMQProtocolDriver(this, conn))
  def start(args: Array[String]) = {
    log.info("Starting Qbert ...")

    connector.start

    log.info("Qbert started ...")
  }

  def stop() = {
    log.info("Stopping Qbert ...")

    connector.stop

    log.info("Qbert has been stopped ...")
  }
}
