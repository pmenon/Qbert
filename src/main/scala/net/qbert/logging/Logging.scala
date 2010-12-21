package net.qbert.logging

import org.slf4j.LoggerFactory

trait Logging {
  val logger = LoggerFactory.getLogger(this.toString)

  def log(msg: String, args: Any*) = logger.debug(msg, args.toArray.asInstanceOf[Array[Object]])

  //def logDebug(msg: String, args: Any*) = logger.debug(msg, args.toArray.asInstanceOf[Array[Object]])

  def logDebug(msg: String, args: Any*) = {}

  //def logInfo(msg: String, args: Any*) = logger.info(msg, args.toArray.asInstanceOf[Array[Object]])

  def logInfo(msg: String, args: Any*) = {}
}
