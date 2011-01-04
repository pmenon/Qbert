package net.qbert.network

/**
 * FrameReceiver
 *
 * @author: <a href="www.prashanthmenon.com">Prashanth Menon</a>
 *
 * @version: 11-01-02 6:08 PM (02 01 2011)
 */

trait FrameReceiver {
  def frameReceived(fr: FrameReader): Unit
}