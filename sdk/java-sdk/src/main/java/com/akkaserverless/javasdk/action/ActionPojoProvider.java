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

package com.akkaserverless.javasdk.action;

import com.akkaserverless.javasdk.impl.ProtoDescriptorGenerator;
import com.akkaserverless.javasdk.impl.action.ActionReflectiveRouter;
import com.akkaserverless.javasdk.impl.action.ActionRouter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.protobuf.Descriptors;
import com.google.protobuf.EmptyProto;

import java.util.function.Function;

public class ActionPojoProvider<A extends Action> implements ActionProvider<A> {

  private final Function<ActionCreationContext, A> factory;

  private final ActionOptions options;
  private final Descriptors.FileDescriptor fileDescriptor;
  private final Descriptors.ServiceDescriptor serviceDescriptor;

  public static <A extends Action> ActionPojoProvider<A> of(Class<A> cls, Function<ActionCreationContext, A> factory) {
    try {
      return new ActionPojoProvider<>(cls, factory, ActionOptions.defaults());
    } catch (JsonMappingException e) {
      throw new RuntimeException(e);
    }
  }

  private ActionPojoProvider(Class<A> cls, Function<ActionCreationContext, A> factory, ActionOptions options) throws JsonMappingException {
    this.factory = factory;
    this.options = options;
    this.fileDescriptor = ProtoDescriptorGenerator.generateFileDescriptorAction(cls);
    this.serviceDescriptor = fileDescriptor.findServiceByName(cls.getSimpleName());
  }

  @Override
  public ActionOptions options() {
    return options;
  }

  @Override
  public Descriptors.ServiceDescriptor serviceDescriptor() {
    return serviceDescriptor;
  }

  @Override
  public ActionRouter<A> newRouter(ActionCreationContext context) {
    A action = factory.apply(context);
    return new ActionReflectiveRouter<>(action);
  }


  @Override
  public Descriptors.FileDescriptor[] additionalDescriptors() {
    return new Descriptors.FileDescriptor[] {
        EmptyProto.getDescriptor(),
        fileDescriptor
    };
  }
}