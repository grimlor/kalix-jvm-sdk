/* This code was generated by Akka Serverless tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */
package com.example.fibonacci

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.akkaserverless.scalasdk.action.Action
import com.akkaserverless.scalasdk.action.ActionCreationContext
object FibonacciActionGenerated {
  // tag::generated-action[]
  class FibonacciAction(creationContext: ActionCreationContext) extends AbstractFibonacciAction {  // <1>

    override def nextNumber(number: Number): Action.Effect[Number] = { //<2>
      throw new RuntimeException("The command handler for `NextNumber` is not implemented, yet")
    }
    // end::generated-action[]
    override def nextNumbers(number: Number): Source[Action.Effect[Number], NotUsed] = ???
    override def nextNumberOfSum(numberSrc: Source[Number, NotUsed]): Action.Effect[Number] = ???
    override def nextNumberOfEach(numberSrc: Source[Number, NotUsed]): Source[Action.Effect[Number], NotUsed] = ???
    // tag::generated-action[]
  }
  // end::generated-action[]
}