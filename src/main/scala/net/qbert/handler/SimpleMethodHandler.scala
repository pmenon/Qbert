package net.qbert.handler

import net.qbert.protocol.AMQProtocolSession
import net.qbert.virtualhost.VirtualHostRegistry
import net.qbert.message.MessagePublishInfo
import net.qbert.framing.{AMQP, AMQLongString, AMQShortString, Method}
import net.qbert.logging.Logging
import net.qbert.queue.QueueConfiguration


class SimpleMethodHandler(val session: AMQProtocolSession) extends MethodHandler with Logging {

  def handleMethod(channelId: Int, method: Method) = method.handle(channelId, this)

  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk) = {
    info("Connection.Start received: " + startOk)

    val tuneMessage = session.methodFactory.createConnectionTune(100,100,100)
    val response = tuneMessage.generateFrame(0)

    success(response)
  }


  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk) = {
    info("Connection.TuneOk received: " + tuneOk)
    // do nothing ... for now
    success()
  }

  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open) = {
    info("Connection.Open received: " + connOpen)

    val res = VirtualHostRegistry.get(connOpen.virtualHost.get).map{(host) =>
      session.virtualHost = Some(host)
      val openok = session.methodFactory.createConnectionOpenOk(AMQShortString(""))
      success(openok.generateFrame(0))
    }.getOrElse(error(UnknownVirtualHost(connOpen.virtualHost.get)))

    res

  }

  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open) = {
    info("Channel.Open received: " + channelOpen)

    val c = session createChannel channelId
    val openok = session.methodFactory.createChannelOpenOk(AMQLongString("channel-"+c.channelId.toString))
    val response = openok.generateFrame(channelId)

    success(response)
  }

  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish) = {
    val publishInfo = MessagePublishInfo(publish.exchangeName.get,
                                         publish.routingKey.get,
                                         publish.mandatory,
                                         publish.immediate)
    session.getChannel(channelId).map{_.publishReceived(publishInfo)}.getOrElse {
      info("Channel {} does not exist during basic.publish attempt")
      //error("Channel doesn't not exist")
    }
    success()
  }

  def handleQueueDeclare(channelId: Int, declare: AMQP.Queue.Declare) = {
    val queueConfig = QueueConfiguration(declare.queueName.get, session.virtualHost.get, declare.durable, declare.exclusive, declare.autoDelete)

    session.virtualHost.map(_.createQueue(queueConfig))

    if(!declare.noWait) {
      val res = session.methodFactory.createQueueDeclareOk(declare.queueName, 0, 0)
      success(res.generateFrame(channelId))
    } else
      success()

  }

}