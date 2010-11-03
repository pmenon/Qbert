package net.qbert.state

trait StateAwareProcessor[S] {
  val stateMachine: StateMachine[S,_]

  def when[O](state: S)(f: => O)(otherwise: => O) = if(stateMachine inState state) f else otherwise
}



