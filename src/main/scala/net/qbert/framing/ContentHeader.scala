package net.qbert.framing

import net.qbert.network.{ CanReadFrom, CanWriteTo, FrameReader, FrameWriter }

import java.util.{ Date => JDate }


object BasicProperties extends CanReadFrom[BasicProperties] {
  
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
  import AMQType._

  lazy val params = List[Option[AMQType]](contentType, contentEncoding, headers, deliveryMode, priority,
                    correlationId, replyTo, expiration, messageId,
                    timestamp, typ, userId, appId, clusterId)

  // TODO: we could do some performance enhancements by iterating through the params once
  // to generate both the bitfield props and calcualte the size

  def size() = {
    2 + params.foldLeft(0)( (acc, o) => acc + (if(o.isDefined) o.get.size else 0) )
  }

  def writeTo(fw: FrameWriter) = {
    var props = 0
    val tempFw = new FrameWriter(size() - 2)

    props = params.foldLeft(0)( (acc, o) => (acc << 1) | (if(o.isDefined) 1 else 0) )
    params.foreach(x => x.map(_.writeTo(tempFw)))

    fw.writeShort(props)
    fw.writeFrom(tempFw)
  }
}

object ContentHeader extends CanReadFrom[ContentHeader] {
  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    new ContentHeader(fr.readShort, fr.readShort, fr.readLongLong, BasicProperties(fr))
  }
}

case class ContentHeader(classId: Short, weight: Short, bodySize: Long, props: BasicProperties) extends FramePayload {
  val typeId = Frame.FRAME_CONTENT

  def size() = 2 + 2 + 8 + props.size

  def writeTo(fw: FrameWriter) = {
    fw.writeShort(classId)
    fw.writeShort(weight)
    fw.writeLongLong(bodySize)
    fw.writeBasicProperties(props)
  }

  //def contentType
}

