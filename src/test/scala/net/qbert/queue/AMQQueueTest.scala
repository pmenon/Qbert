package net.qbert.queue

import net.qbert.channel.AMQChannel
import net.qbert.framing.{ BasicProperties, ContentBody, ContentHeader }
import net.qbert.message.{ AMQMessage, MessagePublishInfo }
import net.qbert.subscription.Subscription
import net.qbert.virtualhost.AMQVirtualHost

import org.specs._
import org.specs.mock.Mockito

class AMQQueueTest extends Specification with Mockito {

  "A Queue" should {
    val host = AMQVirtualHost("/")

    "be able to enqueue" in {
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

      // stop the queue
      queue.close()
    }

    "persist itself if durable" in {
      // create a persistent queue
      val persistentQueue = QueueFactory.createQueue(QueueConfiguration("q1", host, true, true, true))
      val simpleQueue = QueueFactory.createQueue(QueueConfiguration("q2", host, false, false, false))

      host.store.retrieveQueue(persistentQueue) must beSome[AMQQueue].which(_.name.equals("q1"))
      host.store.retrieveQueue(simpleQueue) must beNone

      persistentQueue.close()
      simpleQueue.close()
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
      val sub1 = Subscription("tag1", channel1, queue)
      val sub2 = Subscription("tag2", channel2, queue)
      val sub3 = Subscription("tag3", channel3, queue)

      // channel1 always fails
      channel1.onEnqueue(any, any) returns false
      // channel2 succeeds
      channel2.onEnqueue(any, any) returns true
      // channel3 always fails
      channel3.onEnqueue(any, any) returns false

      // subscribe all channels
      queue.addSubscription(sub1)
      queue.addSubscription(sub2)
      queue.addSubscription(sub3)
      
      // enqueue the message
      queue.enqueue(m)

      // expect each channel except #3 to get one call
      there was one(channel1).onEnqueue(any, any)
      there was one(channel2).onEnqueue(any, any)
      there was no(channel3).onEnqueue(any, any)

      // stop the queue and channels
      queue.close()
      channel1.stop()
      channel2.stop()
      channel3.stop()

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
      channel.onEnqueue(any, any) returns true

      simpleQueue.addSubscription(Subscription("tag1", channel, simpleQueue))

      // the queue should return true since it delivered the message to the sole subscription/channel
      simpleQueue.enqueueAndWait(m).apply().delivered must beTrue

      // stop the queue
      simpleQueue.close()
      channel.stop()
    }
  }

}
