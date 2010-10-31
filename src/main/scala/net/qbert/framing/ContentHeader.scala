package net.qbert.framing

import net.qbert.network.{ CanReadFrom, CanWriteTo, FrameReader, FrameWriter }

import java.util.{ Date => JDate }


object BasicProperties extends CanReadFrom[BasicProperties] {
  //type Spitter = Function1[FrameReader, Option[T]]
  //type Spitter = PartialFunction[FrameReader, Any]
  
  class RicherBoolean(b: Boolean) {
    def option[T](f: => T):Option[T] = if(b) Some(f) else None
  }

  implicit def booleanToRicherBoolean(b: Boolean) = new RicherBoolean(b)

  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    val props = fr.readShort
    val basicProps = 0 to 14 map( i => ((props >> i) & 1) != 0 )
    new BasicProperties(basicProps(13) option fr.readShortString,
                        basicProps(12) option fr.readShortString,
                        basicProps(11) option fr.readFieldTable,
                        basicProps(10) option fr.readOctet,
                        basicProps(9) option fr.readOctet,
                        basicProps(8) option fr.readShortString,
                        basicProps(7) option fr.readShortString,
                        basicProps(6) option fr.readShortString,
                        basicProps(5) option fr.readShortString,
                        basicProps(4) option fr.readTimestamp,
                        basicProps(3) option fr.readShortString,
                        basicProps(2) option fr.readShortString,
                        basicProps(1) option fr.readShortString,
                        basicProps(0) option fr.readShortString)
  }
                               
}

case class BasicProperties(contentType: Option[AMQShortString], contentEncoding: Option[AMQShortString], 
                           headers: Option[AMQFieldTable], deliveryMode: Option[Byte], priority: Option[Byte], 
                           correlationId: Option[AMQShortString], replyTo: Option[AMQShortString], 
                           expiration: Option[AMQShortString], messageId: Option[AMQShortString],
                           timestamp: Option[JDate], typ: Option[AMQShortString], 
                           userId: Option[AMQShortString], appId: Option[AMQShortString], 
                           clusterId: Option[AMQShortString]) extends CanWriteTo {
  def size() = {
    var s = 0

    contentType foreach { s += _.size }
    contentEncoding foreach { s += _.size }
    headers foreach { s += _.size }
    if(deliveryMode isDefined) s += 2
    if(priority isDefined) s += 2
    correlationId foreach { s += _.size }
    replyTo foreach { s += _.size }
    expiration foreach { s += _.size }
    messageId foreach { s += _.size }
    if(timestamp isDefined) s += 8 
    typ foreach { s += _.size }
    userId foreach { s += _.size }
    appId foreach { s += _.size }
    clusterId foreach { s += _.size }
    
    s + 2
  }

  def fold[T](o: Option[T], f: T => Unit, otherwise: () => Unit):Unit = o match {
    case Some(x: T) => f(x)
    case None => otherwise()
  }

  def writeTo(fw: FrameWriter) = {
    var props = 0
    val tempFw = new FrameWriter(size() - 2)
    fold[AMQShortString](contentType, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](contentEncoding, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQFieldTable](headers, {x => props = (props << 1) | 1; tempFw.writeFieldTable(x)}, () => props <<= 1)
    fold[Byte](deliveryMode, {x => props = (props << 1) | 1; tempFw.writeOctet(x)}, () => props <<= 1)
    fold[Byte](priority, {x => props = (props << 1) | 1; tempFw.writeOctet(x)}, () => props <<= 1)
    fold[AMQShortString](correlationId, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](replyTo, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](expiration, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](messageId, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[JDate](timestamp, {x => props = (props << 1) | 1; tempFw.writeTimestamp(x)}, () => props <<= 1)
    fold[AMQShortString](typ, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](userId, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](appId, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)
    fold[AMQShortString](clusterId, {x => props = (props << 1) | 1; tempFw.writeShortString(x)}, () => props <<= 1)

    fw.writeShort(props)
    fw.writeFrom(tempFw)
  }
}


object ContentHeader extends CanReadFrom[ContentHeader] {
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    new ContentHeader(fr.readShort, fr.readShort, BasicProperties(fr))
  }
}

case class ContentHeader(classId: Short, weight: Short, props: BasicProperties) extends FramePayload {
  val typeId = Frame.FRAME_CONTENT

  def size() = 2 + 2 + props.size

  def writeTo(fw: FrameWriter) = {
    fw.writeShort(classId)
    fw.writeShort(weight)
    fw.writeBasicProperties(props)
  }

  //def contentType
}

