package net.qbert.queue

import net.qbert.channel.AMQChannel
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
      queue.enqueue(new AMQMessage(1, info, header, body))

      // dequeue the message from the queue
      val m = queue.dequeue()

      m must beSome[AMQMessage]
      
      val m1 = m.get
      m1.id must ==(1)
      m1.info.exchangeName must ==("exchange1")
      m1.info.routingKey must ==("route1")
      m1.header.bodySize must ==(message.length)
      m1.body.buffer must ==(message)
    }

    "persist itself if durable" in {
      // create a persistent queue
      val persistentQueue = QueueFactory.createQueue(QueueConfiguration("q1", host, true, true, true))
      val simpleQueue = QueueFactory.createQueue(QueueConfiguration("q2", host, false, false, false))

      host.store.retrieveQueue(persistentQueue) must beSome[AMQQueue].which(_.name.equals("q1"))
      host.store.retrieveQueue(simpleQueue) must beNone
    }

    "notify subscribers on enqueue" in {
      val channel1 = mock[AMQChannel]
      val channel2 = mock[AMQChannel]
      val channel3 = mock[AMQChannel]
      val queue = QueueFactory.createQueue(QueueConfiguration("q1", host, true, true, true))
      val sub = Subscription(channel1, queue)

      // subscribe
      queue.subscribe(sub)

      val message = "Hello, world!".getBytes("utf-8")
      val info = MessagePublishInfo("exchange1", "route1", true, true)
      val header = ContentHeader(60, 0, message.length, null)
      val body = ContentBody(message)

      val m = new AMQMessage(1, info, header, body)
      channel1.onEnqueue(m) returns true

      queue.enqueue(m)

      there was one(channel1).onEnqueue(m)

      
    }
  }

}
