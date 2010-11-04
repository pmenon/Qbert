package net.qbert.handler

import net.qbert.connection.{AwaitingConnectionOpen, AwaitingConnectionTuneOk, AwaitingConnectionStartOk, ConnectionState}
import net.qbert.framing.{Method, AMQP}
import net.qbert.state.{StateMachine, StateAwareProcessor}
import net.qbert.protocol.AMQProtocolSession

trait StateAwareMethodHandler extends MethodHandler with StateAwareProcessor[ConnectionState] {
  abstract override def handleConnectionStartOk(channelId: Int, startOk: AMQP.Connection.StartOk) = {
    when(AwaitingConnectionStartOk)(super.handleConnectionStartOk(channelId, startOk))(unexpectedMethodError("StartOK"))
  }
  abstract override def handleConnectionTuneOk(channelId: Int, tuneOk: AMQP.Connection.TuneOk) = {
    when(AwaitingConnectionTuneOk)(super.handleConnectionTuneOk(channelId, tuneOk))(unexpectedMethodError("TuneOk"))
  }
  abstract override def handleConnectionOpen(channelId: Int, open: AMQP.Connection.Open) = {
    when(AwaitingConnectionOpen)(super.handleConnectionOpen(channelId, open))(unexpectedMethodError("Connection.Open"))
  }
}

class StateAwareMethodHandlerImpl(session: AMQProtocolSession, val stateMachine: StateMachine[ConnectionState, Method])
  extends SimpleMethodHandler(session) with StateAwareMethodHandler
