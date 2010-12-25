package net.qbert.framing

import net.qbert.network.{ CanReadIn, CanWriteOut, FrameReader, FrameWriter }

import java.util.{ Date => JDate }


object BasicProperties extends CanReadIn[BasicProperties] {
  /*
  class RicherBoolean(b: Boolean) {
    def optionally[T](f: => T):Option[T] = if(b) Some(f) else None
  }

  class RichTrue

  implicit def booleanToRicherBoolean(b: Boolean) = new RicherBoolean(b)
  */

  def apply(fr: FrameReader) = readFrom(fr)
  def readFrom(fr: FrameReader) = {
    val props = fr.readShort
    val basicProps = 0 to 14 map( i => ((props >> i) & 1) != 0 )
    new BasicProperties(if(basicProps(13)) Some(fr.readShortString) else None,
                        if(basicProps(12)) Some(fr.readShortString) else None,
                        if(basicProps(11)) Some(fr.readFieldTable) else None,
                        if(basicProps(10)) Some(fr.readOctet) else None,
                        if(basicProps(9)) Some(fr.readOctet) else None,
                        if(basicProps(8)) Some(fr.readShortString) else None,
                        if(basicProps(7)) Some(fr.readShortString) else None,
                        if(basicProps(6)) Some(fr.readShortString) else None,
                        if(basicProps(5)) Some(fr.readShortString) else None,
                        if(basicProps(4)) Some(fr.readTimestamp) else None,
                        if(basicProps(3)) Some(fr.readShortString) else None,
                        if(basicProps(2)) Some(fr.readShortString) else None,
                        if(basicProps(1)) Some(fr.readShortString) else None,
                        if(basicProps(0)) Some(fr.readShortString) else None)
  }
                               
}

case class BasicProperties(contentType: Option[AMQShortString], contentEncoding: Option[AMQShortString], 
                           headers: Option[AMQFieldTable], deliveryMode: Option[Byte], priority: Option[Byte], 
                           correlationId: Option[AMQShortString], replyTo: Option[AMQShortString], 
                           expiration: Option[AMQShortString], messageId: Option[AMQShortString],
                           timestamp: Option[JDate], typ: Option[AMQShortString], 
                           userId: Option[AMQShortString], appId: Option[AMQShortString], 
                           clusterId: Option[AMQShortString]) extends CanWriteOut {
  import AMQType._

  lazy val params = List[Option[AMQType]](contentType, contentEncoding, headers, deliveryMode, priority,
                    correlationId, replyTo, expiration, messageId,
                    timestamp, typ, userId, appId, clusterId)

  // TODO: we could do some performance enhancements by iterating through the params once
  // to generate both the bitfield props and calcualte the size
  lazy val (props, propsSize) = params.foldLeft((0,0))( (acc, o) => ( (acc._1 << 1) | (if(o.isDefined) 1 else 0), acc._2 + (if(o.isDefined) o.get.size else 0) ) )

  def size() = {
    2 + propsSize
  }

  def writeTo(fw: FrameWriter) = {
    fw.writeShort(props)
    params.foreach(x => x.map(_.writeTo(fw)))
  }
}

object ContentHeader extends CanReadIn[ContentHeader] {
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

