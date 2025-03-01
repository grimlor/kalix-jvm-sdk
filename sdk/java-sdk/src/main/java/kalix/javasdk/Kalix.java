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

package kalix.javasdk;

import akka.Done;
import akka.actor.ActorSystem;
import kalix.javasdk.action.ActionProvider;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityOptions;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityProvider;
import kalix.javasdk.impl.*;
import kalix.javasdk.impl.action.ActionService;
import kalix.javasdk.impl.action.ResolvedActionFactory;
import kalix.javasdk.impl.eventsourcedentity.EventSourcedEntityService;
import kalix.javasdk.impl.eventsourcedentity.ResolvedEventSourcedEntityFactory;
import kalix.javasdk.impl.replicatedentity.ReplicatedEntityService;
import kalix.javasdk.impl.replicatedentity.ResolvedReplicatedEntityFactory;
import kalix.javasdk.impl.valueentity.ResolvedValueEntityFactory;
import kalix.javasdk.impl.valueentity.ValueEntityService;
import kalix.javasdk.impl.view.ViewService;
import kalix.javasdk.replicatedentity.ReplicatedEntity;
import kalix.javasdk.replicatedentity.ReplicatedEntityOptions;
import kalix.javasdk.replicatedentity.ReplicatedEntityProvider;
import kalix.javasdk.valueentity.ValueEntity;
import kalix.javasdk.valueentity.ValueEntityOptions;
import kalix.javasdk.valueentity.ValueEntityProvider;
import kalix.javasdk.view.ViewProvider;
import kalix.replicatedentity.ReplicatedData;
import com.google.protobuf.Descriptors;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * The Kalix class is the main interface to configuring entities to deploy, and subsequently
 * starting a local server which will expose these entities to the Kalix proxy Sidecar.
 */
public final class Kalix {
  private final Map<String, Function<ActorSystem, Service>> services = new HashMap<>();
  private ClassLoader classLoader = getClass().getClassLoader();
  private String typeUrlPrefix = AnySupport.DefaultTypeUrlPrefix();
  private AnySupport.Prefer prefer = AnySupport.PREFER_JAVA();
  private final LowLevelRegistration lowLevel = new LowLevelRegistration();

  private class LowLevelRegistration {
    /**
     * Register an event sourced entity factory.
     *
     * <p>This is a low level API intended for custom mechanisms for implementing the entity.
     *
     * @param factory The event sourced factory.
     * @param descriptor The descriptor for the service that this entity implements.
     * @param entityType The persistence id for this entity.
     * @param entityOptions the options for this entity.
     * @param additionalDescriptors Any additional descriptors that should be used to look up
     *     protobuf types when needed.
     * @return This stateful service builder.
     */
    public Kalix registerEventSourcedEntity(
        EventSourcedEntityFactory factory,
        Descriptors.ServiceDescriptor descriptor,
        String entityType,
        EventSourcedEntityOptions entityOptions,
        Descriptors.FileDescriptor... additionalDescriptors) {

      AnySupport anySupport = newAnySupport(additionalDescriptors);
      EventSourcedEntityFactory resolvedFactory =
          new ResolvedEventSourcedEntityFactory(
              factory, anySupport.resolveServiceDescriptor(descriptor));

      services.put(
          descriptor.getFullName(),
          system ->
              new EventSourcedEntityService(
                  resolvedFactory,
                  descriptor,
                  additionalDescriptors,
                  anySupport,
                  entityType,
                  entityOptions.snapshotEvery(),
                  entityOptions));

      return Kalix.this;
    }

    /**
     * Register an Action handler.
     *
     * <p>This is a low level API intended for custom mechanisms for implementing the action.
     *
     * @param descriptor The descriptor for the service that this action implements.
     * @param additionalDescriptors Any additional descriptors that should be used to look up
     *     protobuf types when needed.
     * @return This Kalix builder.
     */
    public Kalix registerAction(
        ActionFactory actionFactory,
        Descriptors.ServiceDescriptor descriptor,
        Descriptors.FileDescriptor... additionalDescriptors) {

      final AnySupport anySupport = newAnySupport(additionalDescriptors);

      ActionFactory resolvedActionFactory =
          new ResolvedActionFactory(actionFactory, anySupport.resolveServiceDescriptor(descriptor));

      ActionService service =
          new ActionService(resolvedActionFactory, descriptor, additionalDescriptors, anySupport);

      services.put(descriptor.getFullName(), system -> service);

      return Kalix.this;
    }

    /**
     * Register a value based entity factory.
     *
     * <p>This is a low level API intended for custom mechanisms for implementing the entity.
     *
     * @param factory The value based entity factory.
     * @param descriptor The descriptor for the service that this entity implements.
     * @param entityType The entity type name
     * @param entityOptions The options for this entity.
     * @return This stateful service builder.
     */
    public Kalix registerValueEntity(
        ValueEntityFactory factory,
        Descriptors.ServiceDescriptor descriptor,
        String entityType,
        ValueEntityOptions entityOptions,
        Descriptors.FileDescriptor... additionalDescriptors) {

      AnySupport anySupport = newAnySupport(additionalDescriptors);
      ValueEntityFactory resolvedFactory =
          new ResolvedValueEntityFactory(factory, anySupport.resolveServiceDescriptor(descriptor));

      services.put(
          descriptor.getFullName(),
          system ->
              new ValueEntityService(
                  resolvedFactory,
                  descriptor,
                  additionalDescriptors,
                  anySupport,
                  entityType,
                  entityOptions));

      return Kalix.this;
    }

    /**
     * Register a replicated entity factory.
     *
     * <p>This is a low level API intended for custom mechanisms for implementing the entity.
     *
     * @param factory The replicated entity factory.
     * @param descriptor The descriptor for the service that this entity implements.
     * @param entityType The entity type name.
     * @param entityOptions The options for this entity.
     * @return This stateful service builder.
     */
    public Kalix registerReplicatedEntity(
        ReplicatedEntityFactory factory,
        Descriptors.ServiceDescriptor descriptor,
        String entityType,
        ReplicatedEntityOptions entityOptions,
        Descriptors.FileDescriptor... additionalDescriptors) {

      AnySupport anySupport = newAnySupport(additionalDescriptors);
      ReplicatedEntityFactory resolvedFactory =
          new ResolvedReplicatedEntityFactory(
              factory, anySupport.resolveServiceDescriptor(descriptor));

      services.put(
          descriptor.getFullName(),
          system ->
              new ReplicatedEntityService(
                  resolvedFactory,
                  descriptor,
                  additionalDescriptors,
                  anySupport,
                  entityType,
                  entityOptions));

      return Kalix.this;
    }

    /**
     * Register a view factory.
     *
     * <p>This is a low level API intended for custom mechanisms for implementing the view.
     *
     * @param factory The view factory.
     * @param descriptor The descriptor for the service that this entity implements.
     * @param viewId The id of this view, used for persistence.
     * @param additionalDescriptors Any additional descriptors that should be used to look up
     *     protobuf types when needed.
     * @return This stateful service builder.
     */
    private Kalix registerView(
        ViewFactory factory,
        Descriptors.ServiceDescriptor descriptor,
        String viewId,
        Descriptors.FileDescriptor... additionalDescriptors) {

      AnySupport anySupport = newAnySupport(additionalDescriptors);
      ViewService service =
          new ViewService(
              Optional.ofNullable(factory), descriptor, additionalDescriptors, anySupport, viewId);
      services.put(descriptor.getFullName(), system -> service);

      return Kalix.this;
    }
  }

  /**
   * Sets the ClassLoader to be used for reflective access, the default value is the ClassLoader of
   * the Kalix class.
   *
   * @param classLoader A non-null ClassLoader to be used for reflective access.
   * @return This Kalix instance.
   */
  public Kalix withClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  /**
   * Sets the type URL prefix to be used when serializing and deserializing types from and to
   * Protobyf Any values. Defaults to "type.googleapis.com".
   *
   * @param prefix the type URL prefix to be used.
   * @return This Kalix instance.
   */
  public Kalix withTypeUrlPrefix(String prefix) {
    this.typeUrlPrefix = prefix;
    return this;
  }

  /**
   * When locating protobufs, if both a Java and a ScalaPB generated class is found on the
   * classpath, this specifies that Java should be preferred.
   *
   * @return This Kalix instance.
   */
  public Kalix preferJavaProtobufs() {
    this.prefer = AnySupport.PREFER_JAVA();
    return this;
  }

  /**
   * When locating protobufs, if both a Java and a ScalaPB generated class is found on the
   * classpath, this specifies that Scala should be preferred.
   *
   * @return This Kalix instance.
   */
  public Kalix preferScalaProtobufs() {
    this.prefer = AnySupport.PREFER_SCALA();
    return this;
  }

  /**
   * Register a replicated entity using a {@link ReplicatedEntityProvider}. The concrete <code>
   * ReplicatedEntityProvider</code> is generated for the specific entities defined in Protobuf, for
   * example <code>CustomerEntityProvider</code>.
   *
   * <p>{@link ReplicatedEntityOptions} can be defined by in the <code>ReplicatedEntityProvider
   * </code>.
   *
   * @return This stateful service builder.
   */
  public <D extends ReplicatedData, E extends ReplicatedEntity<D>> Kalix register(
      ReplicatedEntityProvider<D, E> provider) {
    return lowLevel.registerReplicatedEntity(
        provider::newRouter,
        provider.serviceDescriptor(),
        provider.entityType(),
        provider.options(),
        provider.additionalDescriptors());
  }

  /**
   * Register a value based entity using a {{@link ValueEntityProvider}}. The concrete <code>
   * ValueEntityProvider</code> is generated for the specific entities defined in Protobuf, for
   * example <code>CustomerEntityProvider</code>.
   *
   * <p>{{@link ValueEntityOptions}} can be defined by in the <code>ValueEntityProvider</code>.
   *
   * @return This stateful service builder.
   */
  public <S, E extends ValueEntity<S>> Kalix register(ValueEntityProvider<S, E> provider) {
    return lowLevel.registerValueEntity(
        provider::newRouter,
        provider.serviceDescriptor(),
        provider.entityType(),
        provider.options(),
        provider.additionalDescriptors());
  }

  /**
   * Register a event sourced entity using a {{@link EventSourcedEntityProvider}}. The concrete
   * <code>
   * EventSourcedEntityProvider</code> is generated for the specific entities defined in Protobuf,
   * for example <code>CustomerEntityProvider</code>.
   *
   * <p>{{@link EventSourcedEntityOptions}} can be defined by in the <code>
   * EventSourcedEntityProvider</code>.
   *
   * @return This stateful service builder.
   */
  public <S, E extends EventSourcedEntity<S>> Kalix register(
      EventSourcedEntityProvider<S, E> provider) {
    return lowLevel.registerEventSourcedEntity(
        provider::newRouter,
        provider.serviceDescriptor(),
        provider.entityType(),
        provider.options(),
        provider.additionalDescriptors());
  }

  /**
   * Register a view using a {@link ViewProvider}. The concrete <code>
   * ViewProvider</code> is generated for the specific views defined in Protobuf, for example <code>
   * CustomerViewProvider</code>.
   *
   * @return This stateful service builder.
   */
  public Kalix register(ViewProvider provider) {
    return lowLevel.registerView(
        provider::newRouter,
        provider.serviceDescriptor(),
        provider.viewId(),
        provider.additionalDescriptors());
  }

  /**
   * Register an action using an {{@link ActionProvider}}. The concrete <code>
   * ActionProvider</code> is generated for the specific entities defined in Protobuf, for example
   * <code>CustomerActionProvider</code>.
   *
   * @return This stateful service builder.
   */
  public Kalix register(ActionProvider provider) {
    return lowLevel.registerAction(
        provider::newRouter, provider.serviceDescriptor(), provider.additionalDescriptors());
  }

  /**
   * Starts a server with the configured entities.
   *
   * @return a CompletionStage which will be completed when the server has shut down.
   */
  public CompletionStage<Done> start() {
    return createRunner().run();
  }

  /**
   * Starts a server with the configured entities, using the supplied configuration.
   *
   * @return a CompletionStage which will be completed when the server has shut down.
   */
  public CompletionStage<Done> start(Config config) {
    return createRunner(config).run();
  }

  /**
   * Creates a KalixRunner using the currently configured services. In order to start the server,
   * `run()` must be invoked on the returned KalixRunner.
   *
   * @return a KalixRunner
   */
  public KalixRunner createRunner() {
    return new KalixRunner(services);
  }

  /**
   * Creates a KalixRunner using the currently configured services, using the supplied
   * configuration. In order to start the server, `run()` must be invoked on the returned
   * KalixRunner.
   *
   * @return a KalixRunner
   */
  public KalixRunner createRunner(Config config) {
    return new KalixRunner(services, config);
  }

  private AnySupport newAnySupport(Descriptors.FileDescriptor[] descriptors) {
    return new AnySupport(descriptors, classLoader, typeUrlPrefix, prefer);
  }
}
