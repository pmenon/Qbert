package net.qbert.util

import akka.actor.ActorRef
import akka.dispatch.Future

trait ActorDelegate {
  def invokeNoResult[T](actor: ActorRef, m: T): Unit = actor ! m
  def invoke[T, R](actor: ActorRef, m: T): Future[R] = actor !!! m
}