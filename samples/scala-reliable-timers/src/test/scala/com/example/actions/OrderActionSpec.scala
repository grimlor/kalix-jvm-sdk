package com.example.actions

import com.google.protobuf.empty.Empty
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class OrderActionSpec extends AnyWordSpec with Matchers with ScalaFutures {

  "OrderAction" must {

    "handle command PlaceOrder" in {
      val service = OrderActionTestKit(new OrderAction(_))
      val result = service.placeOrder(OrderRequest(item = "Pizza Margherita", quantity = 3))

      val timer = result.nextSingleTimerDetails[OrderNumber, Empty]
      timer.name should startWith("order-expiration-timer")
    }

  }
}
