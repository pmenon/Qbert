package net.qbert.state

import scala.collection.mutable

trait StateAware {
  val stateManager: StateManager
}

trait StateAwareProcessor extends StateAware {
  def runIfInState(s: State)(f: => Boolean) = stateManager.executeIfInState(s)(f)
}

trait StateDriven extends StateAware {
  val initialState: State
  lazy val stateManager = new StateManager(initialState)  
}

trait StateMachine[S, I] {
  var currentState: State = _

  type StateTransitionFunction = PartialFunction[Event, State]

  case class State(name: S)

  case class Event(input: I)

  def when(stateName: S)(f: StateTransitionFunction) = registerTransition(stateName, f)

  def goTo(stateName: S): State = State(stateName)

  def stay() = currentState

  private var transitionMap = new mutable.HashMap[S, StateTransitionFunction]
  def registerTransition(stateName: S, sf: StateTransitionFunction) = {
    transitionMap.put(stateName, sf)
  }

  def actOn(event: Event) = {
    val nextState = transitionMap.get(currentState.name).orElse( Some(handleEvent(_)) ).get.apply(event)
    setState(nextState)
  }

  def handleEvent(e: Event): State

  def setState(newState: State) = currentState = newState
}



