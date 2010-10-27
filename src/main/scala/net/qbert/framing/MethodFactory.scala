package net.qbert.framing

import net.qbert.logging.Logging
import net.qbert.network.FrameReader

trait MethodFactory {
  def createMethodFrom(fr: FrameReader): Option[FramePayload]
  //def createMethodWith(classId: Int, methodId: Int): Option[FramePayload]
}

class MethodFactory_091 extends MethodFactory with Logging {
  def createMethodFrom(fr: FrameReader) = {
    val classId = fr.readShort
    val methodId = fr.readShort

    val method = (classId, methodId) match {
      case (10, 10) => Some(AMQP.Connection.Start(fr))
      case (10, 11) => Some(AMQP.Connection.StartOk(fr))
      case (10, 30) => Some(AMQP.Connection.Tune(fr))
      case (10, 31) => Some(AMQP.Connection.TuneOk(fr))
      case (10, 40) => Some(AMQP.Connection.Open(fr))
      case (10, 41) => Some(AMQP.Connection.OpenOk(fr))

      case (20, 10) => Some(AMQP.Channel.Open(fr))
      case (20, 11) => Some(AMQP.Channel.OpenOk(fr))

      case _ => info("No method matches classId={} methodId={}", classId, methodId); None
    }

    method
  }
}
