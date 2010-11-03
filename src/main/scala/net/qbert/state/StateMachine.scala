package net.qbert.state

import scala.collection.mutable

// borrowed from Akka's FSM
trait StateMachine[S, I] {
  var currentState: S = _

  def when(state: S)(f: (I) => S) = registerStateTransition(state, f)

  def unhandledEvent(input: I) = {
    println("Unhandled event in state " + currentState + " with input " + input)
    error("Error")
  }

  def actOn(input: I) = {
    val nextState = transitionMap.get(currentState).getOrElse( (e) => unhandledEvent(e) ).apply(input)
    setState(nextState)
  }

  private var transitionMap = mutable.Map[S, I => S]()
  def registerStateTransition(state: S, f: I => S) = {
    transitionMap.put(state, f)
  }

  def goTo(state: S) = state

  def setState(nextState: S) = currentState = nextState

  def inState(otherState: S): Boolean = currentState == otherState
  def notInState(otherState: S) = !inState(otherState)
}

    
