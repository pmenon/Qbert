package net.qbert.state

import scala.collection.mutable

trait StateAwareProcessor[S] {
  val stateMachine: StateMachine[S,_]
  //def runIfInState(s: State)(f: => Boolean) = stateManager.executeIfInState(s)(f)
  def when[O](state: S)(f: => O)(otherwise: => O) = if(stateMachine inState state) f else otherwise
}



