import org.specs._

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer

class QbertNetworkTest extends Specification {

  class Test extends Thread {

    override def run = {
      val cf1 = new ConnectionFactory
      cf1.setHost("localhost")
      cf1.setPort(5672)
      cf1.setVirtualHost("/")

      val c1 = cf1.newConnection()
      val ch1 = c1.createChannel
      val consumer1 = new QueueingConsumer(ch1)
      ch1.basicConsume("queue1", true, consumer1)

      println(new String(consumer1.nextDelivery().getBody(), "utf-8"))
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
      c.createChannel()
      c.createChannel()
      c.createChannel()

      ch.queueDeclare("queue1", false, false, false, new java.util.HashMap)
      ch.exchangeDeclare("exchange1", "direct")
      ch.queueBind("queue1", "exchange1", "route1")


      new Test().start()
      Thread.sleep(1000)
      ch.txSelect

      ch.basicPublish("exchange1", "route1", null, "Hello, world1!".getBytes("utf-8"))
      ch.basicPublish("exchange100", "route1", true, true, null, "Hello, world2!".getBytes("utf-8"))


      
      
      ch.txCommit()
      //*/
    }
  }
} 
