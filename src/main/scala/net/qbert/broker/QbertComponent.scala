package net.qbert.broker

/**
 * QbertComponent
 *
 * @author: <a href="www.prashanthmenon.com">Prashanth Menon</a>
 *
 * @version: 11-01-02
 */

trait QbertComponent {
  def start(): Boolean
  def stop(): Boolean
}