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

package com.akkaserverless.javasdk.impl

import scala.jdk.CollectionConverters.ListHasAsScala

import akkaserverless.javasdk.action.EchoAction
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ProtoDescriptorGeneratorSpec extends AnyWordSpecLike with Matchers {

  "ProtoDescriptorGenerator" should {
    "generate a descriptor from a POJO" in {
      val descriptor = ProtoDescriptorGenerator.generateFileDescriptorAction(classOf[EchoAction])

      // very simplistic test to start with
      val service = descriptor.getServices.get(0)
      service.getName shouldBe "EchoAction"

      service.getMethods.size() shouldBe 2

      val sortedMethods = service.getMethods.asScala.sortBy(_.getFullName)

      {
        val method = sortedMethods.head
        method.getInputType.getFullName shouldBe "akkaserverless.javasdk.action.Message"
        method.getOutputType.getFullName shouldBe "akkaserverless.javasdk.action.Message"
      }

      {
        val method = sortedMethods(1)
        method.getInputType.getFullName shouldBe "akkaserverless.javasdk.action.Number"
        method.getOutputType.getFullName shouldBe "akkaserverless.javasdk.action.Number"
      }

    }
  }
}
