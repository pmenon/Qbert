package net.qbert.state

import scala.collection.mutable

// borrowed from Akka's FSM
trait StateMachine[S, I] {
  type StateTransitionFunction = PartialFunction[I, S]

  var currentState: S = _

  def when(state: S)(f: StateTransitionFunction) = registerStateTransition(state, f)

  def whenUnhandled(input: I) = {
    println("Unhandled event in state " + currentState + " with input " + input)
    error("ff")
  }

  def actOn(input: I) = {
    val nextState = transitionMap.get(currentState).getOrElse( whenUnhandled(_) ).apply(input)
    setState(nextState)
  }

  private var transitionMap = mutable.Map[S, StateTransitionFunction]()
  def registerStateTransition(state: S, f: StateTransitionFunction) = {
    transitionMap.put(state, f)
  }

  def goTo(state: S) = state

  def stay = currentState

  def setState(nextState: S) = currentState = nextState

  def inState(otherState: S): Boolean = currentState == otherState
  def notInState(otherState: S) = !inState(otherState)
}

    
