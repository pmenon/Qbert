package net.qbert.state

trait StateAware {
  val stateManager: StateManager
}

trait StateAwareProcessor extends StateAware {
  def runIfInState(s: State)(f: => Boolean) = stateManager.executeIfInState(s)(f)
}

trait StateDriven extends StateAware {
  val initialState: State
  val stateManager = new StateManager(initialState)  
}

