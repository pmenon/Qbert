package net.qbert.state

object State {
  val waitingConnection = WaitingConnection()
  val connecting = Connecting()
  val tuning = Tuning()
  val opening = Opening()
  val opened = Opened()
  val awaitingContentBody = AwaitingContentBody()
}

sealed abstract class State {
  def next(): State
}
case class WaitingConnection() extends State {
  def next() = State.connecting
}
case class Connecting() extends State {
  def next() = State.tuning
}
case class Tuning() extends State{
  def next() = State.opening
}
case class Opening() extends State {
  def next() = State.opened
}
case class Opened() extends State {
  def next() = State.opened
}
case class AwaitingContentHeader() extends State {
  def next() = State.awaitingContentBody
}
case class AwaitingContentBody() extends State {
  def next() = State.opened
}

class StateManager() {
  private var state: State = State.waitingConnection

  def changeState(newState: State) = state = newState
  def nextNaturalState() = state = state next

  def executeIfInState(s: State)(f: => Boolean) = if(inState(s) && f) nextNaturalState

  def inState(otherState: State): Boolean = state eq otherState
  def notInState(otherState: State) = !inState(otherState)
}
