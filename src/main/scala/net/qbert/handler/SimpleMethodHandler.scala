package net.qbert.handler

import net.qbert.protocol.AMQProtocolSession
import net.qbert.virtualhost.VirtualHostRegistry
import net.qbert.message.MessagePublishInfo
import net.qbert.framing.{AMQP, AMQLongString, AMQShortString, Method}
import net.qbert.logging.Logging
import net.qbert.queue.QueueConfiguration
import net.qbert.exchange.ExchangeConfiguration


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

    val c = session.createChannel(channelId)
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

  def handleExchangeDeclare(channelId: Int, declare: AMQP.Exchange.Declare) = {
    val exchangeConfig = ExchangeConfiguration(declare.exchangeName.get, declare.exchangeType.get, declare.durable, declare.autoDelete, declare.internal)
    
    session.virtualHost.map(_.createExchange(exchangeConfig))
    
    if(!declare.noWait) {
      val res = session.methodFactory.createExchangeDeclareOk()
      success(res.generateFrame(channelId))
    } else {
      success()
    }
  }

  def handleQueueDeclare(channelId: Int, declare: AMQP.Queue.Declare) = {
    val queueConfig = QueueConfiguration(declare.queueName.get, session.virtualHost.get, declare.durable, declare.exclusive, declare.autoDelete)

    session.virtualHost.map(_.createQueue(queueConfig))

    if(!declare.noWait) {
      val res = session.methodFactory.createQueueDeclareOk(declare.queueName, 0, 0)
      success(res.generateFrame(channelId))
    } else {
      success()
    }
  }

  def handleQueueBind(channelId: Int, bind: AMQP.Queue.Bind) = {
    val ex = session.virtualHost.map(_.lookupExchange(bind.exchangeName.get)).getOrElse(None)
    val q = session.virtualHost.map(_.lookupQueue(bind.queueName.get)).getOrElse(None)

    val res = (ex, q) match {
      case (Some(exchange), Some(queue)) =>
        exchange.bind(queue, bind.routingKey.get)
        val res = session.methodFactory.createQueueBindOk()
        if(!bind.noWait) success(res.generateFrame(channelId)) else success()
      case (None, Some(_)) => error(QueueDoesNotExist(bind.queueName.get))
      case _ => error(ExchangeDoesNotExist(bind.exchangeName.get))
    }

    res
       
    /*
    ex.map( (exchange) =>
      q.map( (queue) =>
        exchange.bind(queue, bind.routingKey.get)
        val res = session.methodFactory.createQueueBindOk()
        success(res.generateFrame(channelId))
      ).getOrElse( error(QueueDoesNotExist(bind.queueName)) )
    ).getOrElse( error(ExchangeDoesNotExist(bind.exchangeName)) )
    */

  }

}
