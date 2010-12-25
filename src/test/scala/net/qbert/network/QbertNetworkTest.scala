import org.specs._

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer

class QbertNetworkTest extends Specification {

  class ConsumingThread(queueName: String, expectedMsgCount: Int) extends Thread {

    override def run = {
      val cf1 = new ConnectionFactory
      cf1.setHost("localhost")
      cf1.setPort(5672)
      cf1.setVirtualHost("/")

      val c1 = cf1.newConnection()
      val ch1 = c1.createChannel
      val consumer1 = new QueueingConsumer(ch1)
      val tag = ch1.basicConsume(queueName, true, consumer1)

      val t1 = System.nanoTime
      for(i <- 1 to expectedMsgCount) {
        consumer1.nextDelivery()
      }
      val t2 = System.nanoTime
      println("consumer done in " + (t2-t1) + " nano seconds")
    }
  }


  "Simple Qbert connection" should {
    "succeed protocol negotiation" in {
      
      //val broker = new QbertBroker()
      //broker.start(Array())

      val cf = new ConnectionFactory
      cf.setHost("localhost")
      cf.setPort(5672)
      cf.setVirtualHost("/")

      val c = cf.newConnection()
      val ch = c.createChannel()

      val queueName = "queue1"
      val exchangeName = "exchange1"
      val routeName = "route1"

      ch.queueDeclare(queueName, false, false, false, new java.util.HashMap)
      ch.exchangeDeclare(exchangeName, "direct")
      ch.queueBind(queueName, exchangeName, routeName)

      val expectedMsgCount = 200000
      val consumer = new ConsumingThread(queueName, expectedMsgCount)
      consumer.start()
      Thread.sleep(1000)

      val msg = "Hello, world1!".getBytes("utf-8")
      val t0 = System.nanoTime
      for(i <- 1 to expectedMsgCount) {
        ch.basicPublish(exchangeName, routeName, null, msg)
      }
      val duration = System.nanoTime - t0

      consumer.join()

      println("\n\n TPS = " + expectedMsgCount/(duration/1000000000.0) + "\n\n")

      //broker.stop()
    }
  }
} 
