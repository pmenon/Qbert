package net.qbert.handler

import net.qbert.channel.ChannelManager
import net.qbert.connection.AMQConnection
import net.qbert.state.{State, StateManager}
import net.qbert.framing.{AMQP, FramePayload, Method, AMQLongString, AMQShortString}
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolDriver

// 
trait MethodHandler {
  def handleMethod(channelId: Int, payload: FramePayload): Unit
  
  
  def handleConnectionStart(method: Method): Unit
  def handleConnectionStartOk(method: Method): Unit
  def handleConnectionTune(method: Method): Unit
  def handleConnectionTuneOk(method: Method): Unit
  def handleConnectionOpen(method: Method): Unit
  def handleConnectionOpenOk(method: Method): Unit
  def handleChannelOpen(method: Method): Unit
  def handleChannelOpenOk(method: Method): Unit

}

trait StateAwareProcessor {self: {val stateManager: StateManager} =>
  class RunInState(f: Unit) {
    def whenInState(s: State) = if(stateManager inState s) f
  }

  def execute(f: => Unit): RunInState = new RunInState(f)
}

/* We opt to use a simple pattern matching system over
 * a traditional dynamic dispatch or visitor pattern for several
 * reasons: its idiomatic, involves less code and allows versioned protocol
 * method handlers to interfere less wit the method hierarchy
 */
class StateAwareMethodHandler(val protocolDriver: AMQProtocolDriver) extends MethodHandler with StateAwareProcessor with Logging {
  val stateManager = protocolDriver.stateManager
  //def handleMethod(method: Method) = method.handle(this)
  def handleMethod(channelId: Int, payload: FramePayload): Unit = payload match {
    case startok @ AMQP.Connection.StartOk(_,_,_,_) => handleConnectionStartOk(startok)
    case tuneok @ AMQP.Connection.TuneOk(_,_,_) => handleConnectionTuneOk(tuneok)
    case open @ AMQP.Connection.Open(_,_,_) => handleConnectionOpen(open)
    case open @ AMQP.Channel.Open(_) => handleChannelOpen(channelId, open)
    //case publish @ AMQP.Basic.Publish(_,_,_,__) => handleBasicPublish(publish)
    case _ => println("uh oh")
  }

  def handleConnectionStart(method: Method) = { }
  def handleConnectionStartOk(method: Method) = execute {doConnectionStartOk(method)} whenInState State.connecting
  def doConnectionStartOk(method: Method) = {
    if (stateManager notInState State.connecting) error("error")

    val tuneMessage = AMQP.Connection.Tune(100, 100, 100)
    val response = tuneMessage.generateFrame(0)
    protocolDriver writeFrame response

    stateManager nextNaturalState
  }
  def handleConnectionTune(method: Method) = { }
  def handleConnectionTuneOk(method: Method) = { 
    if (stateManager notInState State.tuning) error("error1")
    stateManager nextNaturalState
  }
  def handleConnectionOpen(method: Method) = {
    if(stateManager notInState State.opening) error("error")

    val openok = AMQP.Connection.OpenOk(AMQShortString(""))
    val response = openok.generateFrame(0)
    protocolDriver writeFrame response

    stateManager nextNaturalState()
  }
  def handleConnectionOpenOk(method: Method) = {}
  /*
  def handleChannelOpen(method: Method) = {
    if(stateManager notInState State.opened) error("channel needs to be open")

    val c = protocolDriver createChannel
    val openok = AMQP.Channel.OpenOk(AMQLongString(c.channelId.toString))
    val response = openok generateFrame c.channelId
    protocolDriver writeFrame response
  }
  */
  // TODO: Needs to go !
  def handleChannelOpen(method: Method) = {}
  def handleChannelOpen(channelId: Int, method: Method) = stateManager.executeIfInState(State.opened) {
    val c = protocolDriver createChannel channelId 
    val openok = AMQP.Channel.OpenOk(AMQLongString("queue-"+c.channelId.toString))
    val response = openok.generateFrame(channelId)
    protocolDriver writeFrame response
    true
  }
  def handleChannelOpenOk(method: Method) = {}
}
