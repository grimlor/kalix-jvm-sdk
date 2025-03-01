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

package kalix.scalasdk.impl.view

import kalix.javasdk
import kalix.scalasdk.view.View

private[scalasdk] object ViewUpdateEffectImpl {
  sealed trait PrimaryUpdateEffect[S] extends View.UpdateEffect[S] {
    def toJavaSdk: javasdk.impl.view.ViewUpdateEffectImpl.PrimaryUpdateEffect[S]
  }

  final case class Update[S](state: S) extends PrimaryUpdateEffect[S] {
    override def toJavaSdk =
      javasdk.impl.view.ViewUpdateEffectImpl.Update(state)
  }

  case object Ignore extends PrimaryUpdateEffect[Nothing] {
    override def toJavaSdk =
      javasdk.impl.view.ViewUpdateEffectImpl.Ignore
        .asInstanceOf[javasdk.impl.view.ViewUpdateEffectImpl.PrimaryUpdateEffect[Nothing]]
  }

  final case class Error[T](description: String) extends PrimaryUpdateEffect[T] {
    override def toJavaSdk =
      javasdk.impl.view.ViewUpdateEffectImpl.Error(description)
  }
}
