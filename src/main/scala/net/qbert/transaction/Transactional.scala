package net.qbert.transaction

import scala.collection.mutable

trait Transactional {
  val journal: TransactionJournal
  //val transaction: TransactionJournal.Transaction
}

trait Committable {
  def commit(): Boolean
}

trait Abortable {
  def abort(): Unit
}

trait ServerAction extends Committable with Abortable

trait TransactionJournal {
  def newTransaction(): Transaction

  trait Transaction {
    def enqueueAction(action: ServerAction): Unit
    def commitTransaction(): Unit
    def abortTransaction(): Unit
  }

}

class InMemoryTransactionJournal extends TransactionJournal {
  def newTransaction() = new InMemoryTransaction

  class InMemoryTransaction extends Transaction {
    private val actions = new mutable.ArrayBuffer[ServerAction]()

    def enqueueAction(action: ServerAction) = actions += action
    def commitTransaction() = {
      var success = true
      actions.takeWhile{(action) => success = action.commit(); success}
      if(!success) abortTransaction
    }
    def abortTransaction() = {
      actions.foreach(_.abort())
    }
  }

}
