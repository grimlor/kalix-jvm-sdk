package org.example.valueentity;

import com.akkaserverless.javasdk.valueentity.ValueEntity;
import com.google.protobuf.Empty;
import org.example.Components;
import org.example.ComponentsImpl;

// This code is managed by Akka Serverless tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

/** A value entity. */
public abstract class AbstractCounter extends ValueEntity<CounterDomain.CounterState> {

  protected final Components components() {
    return new ComponentsImpl(commandContext());
  }

  /** Command handler for "Increase". */
  public abstract Effect<Empty> increase(CounterDomain.CounterState currentState, CounterApi.IncreaseValue increaseValue);

  /** Command handler for "Decrease". */
  public abstract Effect<Empty> decrease(CounterDomain.CounterState currentState, CounterApi.DecreaseValue decreaseValue);

}
