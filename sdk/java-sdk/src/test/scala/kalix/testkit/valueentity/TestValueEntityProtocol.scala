/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kalix.testkit.valueentity

import akka.stream.scaladsl.Source
import akka.stream.testkit.TestPublisher
import akka.stream.testkit.scaladsl.TestSink
import kalix.protocol.value_entity.{ ValueEntitiesClient, ValueEntityStreamIn, ValueEntityStreamOut }
import kalix.testkit.TestProtocol.TestProtocolContext

final class TestValueEntityProtocol(context: TestProtocolContext) {
  private val client = ValueEntitiesClient(context.clientSettings)(context.system)

  def connect(): TestValueEntityProtocol.Connection = new TestValueEntityProtocol.Connection(client, context)

  def terminate(): Unit = client.close()
}

object TestValueEntityProtocol {

  final class Connection(client: ValueEntitiesClient, context: TestProtocolContext) {

    import context.system

    private val in = TestPublisher.probe[ValueEntityStreamIn]()
    private val out = client.handle(Source.fromPublisher(in)).runWith(TestSink.probe[ValueEntityStreamOut])

    out.ensureSubscription()

    def send(message: ValueEntityStreamIn.Message): Connection = {
      in.sendNext(ValueEntityStreamIn(message))
      this
    }

    def expect(message: ValueEntityStreamOut.Message): Connection = {
      out.request(1).expectNext(ValueEntityStreamOut(message))
      this
    }

    def expectNext(): ValueEntityStreamOut.Message = {
      out.request(1).expectNext().message
    }

    def expectFailure(descStartingWith: String): Connection =
      expectNext() match {
        case ValueEntityStreamOut.Message.Failure(failure) if failure.description.startsWith(descStartingWith) =>
          this
        case other => throw new RuntimeException(s"Expected failure starting with [$descStartingWith] but got [$other]")
      }

    def expectClosed(): Unit = {
      out.expectComplete()
      in.expectCancellation()
    }

    def passivate(): Unit = close()

    def close(): Unit = {
      in.sendComplete()
      out.expectComplete()
    }
  }
}
