package net.qbert.util

import scala.collection.mutable

trait Registry[A, B] {
  val registry = new mutable.HashMap[A,B]()

  def get(key: A): Option[B] = registry.get(key)
  def register(key: A, value: B) = registry.put(key, value)
  def unregister(key: A) = registry.remove(key)
}
