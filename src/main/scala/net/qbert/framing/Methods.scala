package net.qbert.framing

object AMQP {
  object Connection {
    trait Start extends Method {
      def majorVersion: Byte
      def minorVersion: Byte
      def serverProperties: AMQFieldTable
      def mechanisms: AMQLongString
      def locales: AMQLongString
    }

    trait StartOk extends Method {
      def clientProperties: AMQFieldTable
      def mechanisms: AMQShortString
      def response: AMQLongString
      def locales: AMQShortString
    }

    trait Tune extends Method {
      def channelMax: Short
      def frameMax: Int
      def heartbeat: Short
    }

    trait TuneOk extends Method {
      def channelMax: Short
      def frameMax: Int
      def heartbeat: Short
    }

    trait Open extends Method {
      def virtualHost: AMQShortString
      def capabilities: AMQShortString
      def insist: Byte
    }

    trait OpenOk extends Method {
      def knownHosts: AMQShortString
    }
  }
  object Channel {
    trait Open extends Method {
      def outOfBand: AMQShortString
    }

    trait OpenOk extends Method {
      def channelId: AMQLongString
    }
  }

  object Basic {
    trait Publish extends Method {
      def ticket: Short
      def exchangeName: AMQShortString
      def routingKey: AMQShortString
      def mandatory: Boolean
      def immediate: Boolean
    }
  }

  object Exchange {
    trait Declare extends Method {
      def ticket: Short
      def exchangeName: AMQShortString
      def exchangeType: AMQShortString
      def passive: Boolean
      def durable: Boolean
      def autoDelete: Boolean
      def internal: Boolean
      def noWait: Boolean
      def args: AMQFieldTable
    }

    trait DeclareOk extends Method 
  }

  object Queue {
    trait Declare extends Method {
      def ticket: Short
      def queueName: AMQShortString
      def passive: Boolean
      def durable: Boolean
      def exclusive: Boolean
      def autoDelete: Boolean
      def noWait: Boolean
    }

    trait DeclareOk extends Method {
      def queueName: AMQShortString
      def messageCount: Int
      def consumerCount: Int
    }

    trait Bind extends Method {
      def ticket: Short
      def queueName: AMQShortString
      def exchangeName: AMQShortString
      def routingKey: AMQShortString
      def noWait: Boolean
      def args: AMQFieldTable
    }

    trait BindOk extends Method
  }
}
