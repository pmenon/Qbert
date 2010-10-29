package net.qbert.handler

import net.qbert.channel.ChannelManager
import net.qbert.connection.AMQConnection
import net.qbert.state.{State, StateAwareProcessor, StateAware, StateManager}
import net.qbert.message.MessagePublishInfo
import net.qbert.framing.{AMQP, Method, MethodFactory, AMQLongString, AMQShortString}
import net.qbert.logging.Logging
import net.qbert.protocol.AMQProtocolSession
import net.qbert.virtualhost.VirtualHostRegistry

object MethodHandler {
  def apply(session: AMQProtocolSession with StateAware) = new StateAwareMethodHandler(session)
}

// 
trait MethodHandler {
  def handleMethod(channelId: Int, method: Method): Unit
  
  
  // handle methods that the server should never see up here
  def handleConnectionStart(start: AMQP.Connection.Start): Unit = {}
  def handleConnectionStartOk(startOk: AMQP.Connection.StartOk): Unit
  def handleConnectionTune(tune: AMQP.Connection.Tune): Unit = {}
  def handleConnectionTuneOk(tuneOk: AMQP.Connection.TuneOk): Unit
  def handleConnectionOpen(connOpen: AMQP.Connection.Open): Unit
  def handleConnectionOpenOk(connOpenOk: AMQP.Connection.OpenOk): Unit = {}
  def handleChannelOpen(channelOpen: AMQP.Channel.Open): Unit
  def handleChannelOpenOk(channelOpenOk: AMQP.Channel.OpenOk): Unit = {}
  def handleBasicPublish(publish: AMQP.Basic.Publish): Unit

}

/* We opt to use a simple pattern matching system over
 * a traditional dynamic dispatch or visitor pattern for several
 * reasons: its idiomatic, involves less code and allows versioned protocol
 * method handlers to interfere less wit the method hierarchy
 */
class StateAwareMethodHandler(val session: AMQProtocolSession with StateAware) extends MethodHandler with StateAwareProcessor with Logging {
  val stateManager = session.stateManager
  val methodFactory = session.methodFactory

  //def handleMethod(method: Method) = method.handle(this)
  def handleMethod(channelId: Int, method: Method): Unit = method match {
    case startok: AMQP.Connection.StartOk => handleConnectionStartOk(startok)
    case tuneok: AMQP.Connection.TuneOk => handleConnectionTuneOk(tuneok)
    case open: AMQP.Connection.Open => handleConnectionOpen(open)
    case open: AMQP.Channel.Open => handleChannelOpen(channelId, open)
    case publish: AMQP.Basic.Publish => handleBasicPublish(channelId, publish)
    case _ => println("uh oh")
  }


  def handleConnectionStartOk(startOk: AMQP.Connection.StartOk) = runIfInState(State.connecting){ 
    info("Connection.Start received: " + startOk)

    val tuneMessage = methodFactory.createConnectionTune(100,100,100)
    val response = tuneMessage.generateFrame(0)
    session writeFrame response

    true
  }


  def handleConnectionTuneOk(tuneOk: AMQP.Connection.TuneOk) = runIfInState(State.tuning){
    // do nothing ... for now
    true
  }

  def handleConnectionOpen(connOpen: AMQP.Connection.Open) = runIfInState(State.opening){

    session.virtualHost = VirtualHostRegistry.get(connOpen.virtualHost.get)
    val openok = methodFactory.createConnectionOpenOk(AMQShortString(""))
    val response = openok.generateFrame(0)
    session writeFrame response

    true
  }

  // TODO: Needs to go !
  def handleChannelOpen(channelOpen: AMQP.Channel.Open) = {}
  def handleChannelOpen(channelId: Int, channelOpen: AMQP.Channel.Open) = runIfInState(State.opened) {
    val c = session createChannel channelId 
    //val openok = AMQP.Channel.OpenOk(AMQLongString("queue-"+c.channelId.toString))
    val openok = methodFactory.createChannelOpenOk(AMQLongString("queue-"+c.channelId.toString))
    val response = openok.generateFrame(channelId)
    session writeFrame response
    true
  }

  // TODO: Needs to go
  def handleBasicPublish(publish: AMQP.Basic.Publish) = {}
  def handleBasicPublish(channelId: Int, publish: AMQP.Basic.Publish) = {
    val publishInfo = MessagePublishInfo(publish.exchangeName.get, publish.routingKey.get, publish.mandatory, publish.immediate)
    session.getChannel(channelId).map{_.publishReceived(publishInfo)}.getOrElse {
      info("Channel {} does not exist during basic.publish attempt")
    }
  }

}
