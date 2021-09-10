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

package com.akkaserverless.javasdk.tck.model.valueentity;

import com.akkaserverless.javasdk.valueentity.ValueEntityContext;
import com.akkaserverless.javasdk.valueentity.ValueEntityOptions;
import com.akkaserverless.javasdk.valueentity.ValueEntityProvider;
import com.akkaserverless.tck.model.ValueEntity;
import com.akkaserverless.tck.model.eventing.LocalPersistenceEventing;
import com.google.protobuf.Descriptors;
import com.google.protobuf.EmptyProto;

import java.util.function.Function;

/** A value entity provider */
public class ValueEntityTckModelEntityProvider
    implements ValueEntityProvider<ValueEntity.Persisted, ValueEntityTckModelEntity> {

  private final Function<ValueEntityContext, ValueEntityTckModelEntity> entityFactory;
  private final ValueEntityOptions options;

  /** Factory method of ShoppingCartProvider */
  public static ValueEntityTckModelEntityProvider of(
      Function<ValueEntityContext, ValueEntityTckModelEntity> entityFactory) {
    return new ValueEntityTckModelEntityProvider(entityFactory, ValueEntityOptions.defaults());
  }

  private ValueEntityTckModelEntityProvider(
      Function<ValueEntityContext, ValueEntityTckModelEntity> entityFactory,
      ValueEntityOptions options) {
    this.entityFactory = entityFactory;
    this.options = options;
  }

  @Override
  public final ValueEntityOptions options() {
    return options;
  }

  public final ValueEntityTckModelEntityProvider withOptions(ValueEntityOptions options) {
    return new ValueEntityTckModelEntityProvider(entityFactory, options);
  }

  @Override
  public final Descriptors.ServiceDescriptor serviceDescriptor() {
    return ValueEntity.getDescriptor().findServiceByName("ValueEntityTckModel");
  }

  @Override
  public final String entityType() {
    return "value-entity-tck-model";
  }

  @Override
  public final ValueEntityTckModelEntityHandler newHandler(ValueEntityContext context) {
    return new ValueEntityTckModelEntityHandler(entityFactory.apply(context));
  }

  @Override
  public final Descriptors.FileDescriptor[] additionalDescriptors() {
    return new Descriptors.FileDescriptor[] {
      ValueEntity.getDescriptor(), EmptyProto.getDescriptor()
    };
  }
}
