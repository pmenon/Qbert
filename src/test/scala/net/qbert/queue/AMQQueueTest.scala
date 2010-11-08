package net.qbert.queue

import net.qbert.channel.AMQChannel
import net.qbert.exchange.{ ExchangeConfiguration, ExchangeTypes }
import net.qbert.framing.{ BasicProperties, ContentBody, ContentHeader }
import net.qbert.message.{ AMQMessage, MessagePublishInfo }
import net.qbert.protocol.AMQProtocolSession
import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost

import org.specs._
import org.specs.mock.Mockito

class AMQQueueTest extends Specification with Mockito {

  "A Queue" should {
    val host = AMQVirtualHost("/")

    "be able to enqueue and dequeue" in {
      // create a simple queue
      val queue = QueueFactory.createQueue(QueueConfiguration("q1", host, false, false, false))

      // create a non persistent message
      val message = "Hello, world!".getBytes("utf-8")
      val info = MessagePublishInfo("exchange1", "route1", true, true)
      val header = ContentHeader(60, 0, message.length, null)
      val body = ContentBody(message)

      // enqueue the message
      val m = new AMQMessage(1, info, header, body)
      val res = queue.enqueue(m)

      // dequeue the message from the queue
      //queue.dequeue(m)

      //m must beSome[AMQMessage]
      /*
      m1.id must ==(1)
      m1.info.exchangeName must ==("exchange1")
      m1.info.routingKey must ==("route1")
      m1.header.bodySize must ==(message.length)
      m1.body.buffer must ==(message)
      */
    }

    "persist itself if durable" in {
      // create a persistent queue
      val persistentQueue = QueueFactory.createQueue(QueueConfiguration("q1", host, true, true, true))
      val simpleQueue = QueueFactory.createQueue(QueueConfiguration("q2", host, false, false, false))

      host.store.retrieveQueue(persistentQueue) must beSome[AMQQueue].which(_.name.equals("q1"))
      host.store.retrieveQueue(simpleQueue) must beNone
    }

    "notify subscribers on enqueue" in {
      // the message
      val message = "Hello, world!".getBytes("utf-8")
      val info = MessagePublishInfo("exchange1", "route1", true, true)
      val header = ContentHeader(60, 0, message.length, BasicProperties(None, None, None, Some(2), None, None, None, 
                                                                        None, None, None, None, None, None, None))
      val body = ContentBody(message)
      val m = new AMQMessage(1, info, header, body)

      val channel1 = mock[AMQChannel]
      val channel2 = mock[AMQChannel]
      val channel3 = mock[AMQChannel]
      val queue = QueueFactory.createQueue(QueueConfiguration("q1", host, true, true, true))
      val sub1 = Subscription(channel1, queue)
      val sub2 = Subscription(channel2, queue)
      val sub3 = Subscription(channel3, queue)

      // channel1 always fails
      channel1.onEnqueue(any) returns false
      // channel2 succeeds
      channel2.onEnqueue(any) returns true
      // channel3 always fails
      channel3.onEnqueue(any) returns false

      // subscribe all channels
      queue.subscribe(sub1)
      queue.subscribe(sub2)
      queue.subscribe(sub3)
      
      // enqueue the message
      queue.enqueue(m)

      // expect each channel except #3 to get one call
      there was one(channel1).onEnqueue(any)
      there was one(channel2).onEnqueue(any)
      there was no(channel3).onEnqueue(any)
    }

    "should notify the acting channel if a message is underliverable" in {
      val simpleQueue = QueueFactory.createQueue(QueueConfiguration("q1", host, false, false, false))
      
      // create an immediate message
      val message = "Hello, world!".getBytes("utf-8")
      val info = MessagePublishInfo("exchange1", "route1", true, true)
      val header = ContentHeader(60, 0, message.length, BasicProperties(None, None, None, Some(2), None, None, None, 
                                                                        None, None, None, None, None, None, None))
      val body = ContentBody(message)
      val m = new AMQMessage(1, info, header, body)
      
      // enqueueing the message and waiting should return false as the message was not delivered
      simpleQueue.enqueueAndWait(m).apply().delivered must beFalse

      // create a mock channel that accepts any message
      val channel = mock[AMQChannel]
      channel.onEnqueue(any) returns true

      simpleQueue.subscribe(Subscription(channel, simpleQueue))

      // the queue should return true since it delivered the message to the sole subscription/channel
      simpleQueue.enqueueAndWait(m).apply().delivered must beTrue

    }
  }

}
