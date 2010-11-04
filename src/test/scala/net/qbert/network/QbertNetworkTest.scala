import org.specs._

import com.rabbitmq.client.ConnectionFactory

class QbertNetworkTest extends Specification {
  "Simple Qbert connection" should {
    "succeed protocol negotiation" in {
      
      Qbert.main(Array())

      val cf = new ConnectionFactory
      cf.setHost("localhost")
      cf.setPort(5672)
      cf.setVirtualHost("/")

      val c = cf.newConnection()
      val ch = c.createChannel()
      c.createChannel()
      c.createChannel()
      c.createChannel()
      
      ch.queueDeclare("test", true, true, true, new java.util.HashMap)
    }
  }
} 
