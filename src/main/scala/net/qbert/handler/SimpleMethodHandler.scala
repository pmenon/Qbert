package net.qbert.handler

import net.qbert.connection.AMQConnection
import net.qbert.protocol.AMQProtocolSession
import net.qbert.virtualhost.VirtualHostRegistry
import net.qbert.message.MessagePublishInfo
import net.qbert.framing.{AMQP, AMQLongString, AMQShortString, Method}
import net.qbert.util.Logging
import net.qbert.queue.QueueConfiguration
import net.qbert.exchange.ExchangeConfiguration
import net.qbert.error.AMQPError


class SimpleMethodHandler(val conn: AMQConnection) extends MethodHandler with Logging {
  lazy implicit val noOp = () => {}

  def handleMethod(channelId: Int, method: Method) = method.handle(channelId, this)

  def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk) = {
    log.info("Connection.Start received: {}", startOk)

    val tuneMessage = conn.methodFactory.createConnectionTune(100,100,100)
    val response = tuneMessage.generateFrame(0)

    success(response)
  }


  def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk) = {
    log.info("Connection.TuneOk received: {}", tuneOk)
    // do nothing ... for now
    success()
  }

  def handleConnectionOpen(channelId: Int, connOpen: AMQP.Connection.Open) = {
    log.info("Connection.Open received: {}", connOpen)

    val res = VirtualHostRegistry.get(connOpen.virtualHost.get).map{(host) =>
      //conn.virtualHost = Some(host)
      val openok = conn.methodFactory.createConnectionOpenOk(AMQShortString(""))
      success(openok.generateFrame(0))
    }.getOrElse(error(UnknownVirtualHost(connOpen.virtualHost.get)))

    res
  }

  def handleConnectionClose(channelId: Int, close: AMQP.Connection.Close) = {
    success()
  }

  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open) = {
    log.info("Channel.Open received: {}", channelOpen)

    val c = conn.createChannel(channelId)
    val openok = conn.methodFactory.createChannelOpenOk(AMQLongString("channel-"+c.channelId.toString))
    val response = openok.generateFrame(channelId)

    success(response)
  }

  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish) = {
    val publishInfo = MessagePublishInfo(publish.exchangeName.get,
                                         publish.routingKey.get,
                                         publish.mandatory,
                                         publish.immediate)
    conn.getChannel(channelId).map{_.publishReceived(publishInfo)}.getOrElse {
      log.info("Channel {} does not exist during basic.publish attempt")
      //error("Channel doesn't not exist")
    }
    success()
  }

  def handleExchangeDeclare(channelId: Int, declare: AMQP.Exchange.Declare) = {
    val exchangeConfig = ExchangeConfiguration(declare.exchangeName.get, declare.exchangeType.get, declare.durable, declare.autoDelete, declare.internal)
    
    conn.virtualHost.createExchange(exchangeConfig)
    
    if(!declare.noWait) {
      val res = conn.methodFactory.createExchangeDeclareOk()
      success(res.generateFrame(channelId))
    } else {
      success()
    }
  }

  def handleQueueDeclare(channelId: Int, declare: AMQP.Queue.Declare) = {
    log.info("Handling Queue.Declare method ...")
    val queueConfig = QueueConfiguration(declare.queueName.get, conn.virtualHost, declare.durable, declare.exclusive, declare.autoDelete)

    conn.virtualHost.createQueue(queueConfig)

    if(!declare.noWait) {
      val res = conn.methodFactory.createQueueDeclareOk(declare.queueName, 0, 0)
      success(res.generateFrame(channelId))
    } else {
      success()
    }
  }

  def handleQueueBind(channelId: Int, bind: AMQP.Queue.Bind) = {

    conn.virtualHost.lookupExchange(bind.exchangeName.get).map( (exchange) =>
      conn.virtualHost.lookupQueue(bind.queueName.get).map{ (queue) =>
        exchange.bind(queue, bind.routingKey.get)
        val res = conn.methodFactory.createQueueBindOk()
        if(!bind.noWait) success(res.generateFrame(channelId)) else success()
      }.getOrElse(error(QueueDoesNotExist(bind.queueName.get)))
    ).getOrElse(error(ExchangeDoesNotExist(bind.exchangeName.get)))

    /*
    val ex = conn.virtualHost.map(_.lookupExchange(bind.exchangeName.get)).getOrElse(None)
    val q = conn.virtualHost.map(_.lookupQueue(bind.queueName.get)).getOrElse(None)

    val res = (ex, q) match {
      case (Some(exchange), Some(queue)) =>
        exchange.bind(queue, bind.routingKey.get)
        val res = conn.methodFactory.createQueueBindOk()
        if(!bind.noWait) success(res.generateFrame(channelId)) else success()
      case (None, Some(_)) => error(QueueDoesNotExist(bind.queueName.get))
      case _ => error(ExchangeDoesNotExist(bind.exchangeName.get))
    }

    res
    */
  }

  def handleBasicConsume(channelId: Int, consume: AMQP.Basic.Consume) = {

    /*
    for {
      channel <- conn.getChannel(channelId)
      host <- conn.virtualHost
      queue <- host.lookupQueue(consume.queueName.get)
    } yield {
      queue.subscribe(Subscription(channel, queue))
      success()
    }
    */

    val res = conn.getChannel(channelId).map( (channel) =>
      conn.virtualHost.lookupQueue(consume.queueName.get).map{ (queue) =>
          val sFun = {(consumerTag: String) =>
            val method = conn.methodFactory.createBasicConsumeOk(consumerTag)
            conn.writeFrame(method.generateFrame(channel.channelId))
          }
          val eFun = {(replyText: String) =>
            val method = conn.methodFactory.createConnectionClose(AMQPError.NOT_ALLOWED, replyText, consume.classId, consume.methodId)
            conn.writeFrame(method.generateFrame(channel.channelId))
          }

          // we return success here and let the channel return an error if any should come up
          channel.subscribeToQueue(consume.consumerTag.get, queue)(sFun, eFun)
          success()

        }.getOrElse(error(QueueDoesNotExist(consume.queueName.get)))
    ).getOrElse(error(ChannelDoesNotExist(channelId)))

    res

  }



}
