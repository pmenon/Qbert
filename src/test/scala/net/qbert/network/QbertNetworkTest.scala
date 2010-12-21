import org.specs._

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer

class QbertNetworkTest extends Specification {

  class ConsumingThread(queueName: String) extends Thread {
    private var going = true

    def stopConsuming() = {
      going = false
    }

    override def run = {
      val cf1 = new ConnectionFactory
      cf1.setHost("localhost")
      cf1.setPort(5672)
      cf1.setVirtualHost("/")

      val c1 = cf1.newConnection()
      val ch1 = c1.createChannel
      val consumer1 = new QueueingConsumer(ch1)
      val tag = ch1.basicConsume(queueName, true, consumer1)

      while(going) {
        //println("---------- " + tag + " ---------------  " + new String(consumer1.nextDelivery().getBody(), "utf-8"))
        consumer1.nextDelivery()
      }
    }
  }


  "Simple Qbert connection" should {
    "succeed protocol negotiation" in {
      
      //Qbert.main(Array())

      val cf = new ConnectionFactory
      cf.setHost("localhost")
      cf.setPort(5672)
      cf.setVirtualHost("/")

      val c = cf.newConnection()
      val ch = c.createChannel()
      //c.createChannel()
      //c.createChannel()
      //c.createChannel()

      ch.queueDeclare("queue1", false, false, false, new java.util.HashMap)
      ch.queueDeclare("queue2", false, false, false, new java.util.HashMap)
      ch.exchangeDeclare("exchange1", "direct")
      ch.queueBind("queue1", "exchange1", "route1")
      ch.queueBind("queue2", "exchange1", "route2")


      val th1 = new ConsumingThread("queue1")
      th1.start()
      Thread.sleep(1000)

      //ch.txSelect
      val t1 = System.currentTimeMillis
      for(i <- 1 to 100000) {
      ch.basicPublish("exchange1", "route1", null, "Hello, world1!".getBytes("utf-8"))
      //ch.basicPublish("exchange1", "route2", true, true, null, "Hello, world2!".getBytes("utf-8"))
      //ch.txCommit()
      }
      val t2 = System.currentTimeMillis

      Thread.sleep(10000)

      th1.stopConsuming()

      println("\n\n TPS = " + 100000/((t2-t1)/1000) + "\n\n")

    }
  }
} 
