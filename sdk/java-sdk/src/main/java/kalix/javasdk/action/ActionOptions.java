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

package kalix.javasdk.action;

import kalix.javasdk.impl.ComponentOptions;
import kalix.javasdk.PassivationStrategy;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityOptions;
import kalix.javasdk.impl.action.ActionOptionsImpl;
import kalix.javasdk.impl.eventsourcedentity.EventSourcedEntityOptionsImpl;

import java.util.Collections;

/** Options for actions */
public interface ActionOptions extends ComponentOptions {

  /** Create default options for an action. */
  static ActionOptions defaults() {
    return new ActionOptionsImpl(Collections.emptySet());
  }

  /**
   * @return the headers requested to be forwarded as metadata (cannot be mutated, use
   *     withForwardHeaders)
   */
  java.util.Set<String> forwardHeaders();

  /**
   * Ask Kalix to forward these headers from the incoming request as metadata headers for the
   * incoming commands. By default, no headers except "X-Server-Timing" are forwarded.
   */
  ComponentOptions withForwardHeaders(java.util.Set<String> headers);
}
