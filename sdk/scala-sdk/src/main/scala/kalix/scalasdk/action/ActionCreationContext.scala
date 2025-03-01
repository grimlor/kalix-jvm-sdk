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

package kalix.scalasdk.action

import kalix.scalasdk.Context

trait ActionCreationContext extends Context {

  /**
   * Get an Akka gRPC client for the given service name. The same client instance is shared across components in the
   * application. The lifecycle of the client is managed by the SDK and it should not be stopped by user code.
   *
   * @tparam T
   *   The "service" interface generated for the service by Akka gRPC
   * @param clientClass
   *   The class of a gRPC service generated by Akka gRPC
   * @param service
   *   The name of the service to connect to, either a name of another Kalix service or an external service where
   *   connection details are configured under `akka.grpc.client.[service-name]` in `application.conf`.
   */
  def getGrpcClient[T](clientClass: Class[T], service: String): T
}
