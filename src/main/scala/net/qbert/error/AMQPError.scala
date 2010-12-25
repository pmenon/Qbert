package net.qbert.error

/**
 * Created by IntelliJ IDEA.
 * User: menonp
 * Date: 10-12-20
 * Time: 8:59 PM
 * To change this template use File | Settings | File Templates.
 */

object AMQPError {
  val REPLY_SUCCESS = 200
  val CONTENT_TOO_LARGE = 311
  val NO_CONSUMERS = 313
  val CONNECTION_FORCED = 320
  val INVALID_PATH = 402
  val ACCESS_REFUSED = 403
  val NOT_FOUND = 404
  val RESOURCE_LOCKED = 405
  val PRECONDITION_FAILED = 406
  val FRAME_ERROR = 501
  val SYNTAX_ERROR = 502
  val COMMAND_INVALID = 503
  val CHANNEL_ERROR = 504
  val UNEXPECTED_FRAME = 505
  val RESOURCE_ERROR = 506
  val NOT_ALLOWED = 540
  val INTERNAL_ERROR = 541
}
