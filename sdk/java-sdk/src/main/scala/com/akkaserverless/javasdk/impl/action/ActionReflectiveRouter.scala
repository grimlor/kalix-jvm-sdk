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

package com.akkaserverless.javasdk.impl.action

import java.lang.reflect.Method

import akka.NotUsed
import akka.stream.javadsl.Source
import com.akkaserverless.javasdk.action.Action
import com.akkaserverless.javasdk.action.MessageEnvelope
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper

object ActionReflectiveRouter {
  case class MethodDesc private[javasdk] (javaMethod: Method) {
    // action's handlers have one single param, always
    final val inputType: Class[_] = javaMethod.getParameterTypes().head
  }
}

class ActionReflectiveRouter[A <: Action](override val action: A) extends ActionRouter[A](action) {

  val allHandlers: Map[String, ActionReflectiveRouter.MethodDesc] =
    // FIXME: names must be unique, overloading shouldn't be allowed,
    //  we should detect it here and fail-fast
    action.getClass.getDeclaredMethods.toList
      // handlers are all methods returning Effect
      .filter(_.getReturnType == classOf[Action.Effect[_]])
      // and with one single input param
      .filter(_.getParameters.length == 1)
      .map { javaMethod => (javaMethod.getName.capitalize, ActionReflectiveRouter.MethodDesc(javaMethod)) }
      .toMap

  private def methodLookup(commandName: String) =
    allHandlers.getOrElse(commandName, throw new RuntimeException(s"no matching method for '$commandName'"))

  override def handleUnary(commandName: String, message: MessageEnvelope[Any]): Action.Effect[_] =
    methodLookup(commandName).javaMethod.invoke(message.payload()).asInstanceOf[Action.Effect[_]]

  override def handleStreamedOut(
      commandName: String,
      message: MessageEnvelope[Any]): Source[Action.Effect[_], NotUsed] = ???

  override def handleStreamedIn(commandName: String, stream: Source[MessageEnvelope[Any], NotUsed]): Action.Effect[_] =
    ???

  override def handleStreamed(
      commandName: String,
      stream: Source[MessageEnvelope[Any], NotUsed]): Source[Action.Effect[_], NotUsed] = ???
}
