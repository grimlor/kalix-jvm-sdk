/* This code was generated by Kalix tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */

package com.example.actions;

import kalix.javasdk.action.ActionCreationContext;
import com.example.domain.CounterDomain;
import com.google.protobuf.Empty;
import com.google.protobuf.Any;

import java.util.Optional;

// tag::counter-topic[]
// tag::counter-ignore[]
public class CounterJournalToTopicAction extends AbstractCounterJournalToTopicAction {
// end::counter-ignore[]
  public CounterJournalToTopicAction(ActionCreationContext creationContext) {}

  // tag::counter-topic-event-subject[]
  @Override
  public Effect<CounterTopicApi.Increased> increase(CounterDomain.ValueIncreased valueIncreased) {
    // end::counter-topic[]
    Optional<String> counterId = actionContext().eventSubject(); //<1>
    // end::counter-topic-event-subject[]
    /*
    // tag::counter-topic-event-subject[]
    ...
    // end::counter-topic-event-subject[]
    */
    // tag::counter-topic[]
    CounterTopicApi.Increased increased = // <1>
      CounterTopicApi.Increased.newBuilder()
        .setValue(valueIncreased.getValue())
        .build();

    return effects().reply(increased); // <2>
  // tag::counter-topic-event-subject[]
  }
  // end::counter-topic-event-subject[]
  // end::counter-topic[]

  @Override
  public Effect<CounterTopicApi.Increased> increaseConditional(CounterDomain.ValueIncreased valueIncreased) {
    Optional<String> counterId = actionContext().eventSubject();

    CounterTopicApi.Increased increased;
    if(actionContext().metadata().get("myKey").equals(Optional.of("myValue")) 
      && actionContext().eventSubject().equals(Optional.of("mySubject"))){
      increased = 
      CounterTopicApi.Increased.newBuilder() 
        .setValue(valueIncreased.getValue() * 2)
        .build();
    } else {
      increased = 
      CounterTopicApi.Increased.newBuilder() 
        .setValue(valueIncreased.getValue())
        .build();
    }
    return effects().reply(increased); 
  } 

  @Override
  public Effect<CounterTopicApi.Decreased> decrease(CounterDomain.ValueDecreased valueDecreased) {
    CounterTopicApi.Decreased decreased =
        CounterTopicApi.Decreased.newBuilder()
            .setValue(valueDecreased.getValue())
            .build();

    return effects().reply(decreased);
  }

  // tag::counter-ignore[]
  @Override
  public Effect<Empty> ignore(Any any) {
    return effects().reply(Empty.getDefaultInstance()); // <1>
  }
  // tag::counter-topic[]
}
  // end::counter-ignore[]
// end::counter-topic[]