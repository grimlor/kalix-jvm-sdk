/* This code was generated by Akka Serverless tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */
package com.example.domain;

import com.akkaserverless.javasdk.testkit.EventSourcedResult;
import com.akkaserverless.javasdk.testkit.DeferredCallDetails;
import com.example.CounterApi;
import com.google.protobuf.Empty;
import org.junit.Test;

import static org.junit.Assert.*;

public class CounterTest {

  @Test
  public void increaseTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    EventSourcedResult<Empty> result = testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(1).build());
    assertTrue(result.didEmitEvents());
    assertEquals(1, testKit.getState().getValue());
  }

  @Test
  public void increaseWithSideEffectTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    int increase = 1;
    EventSourcedResult<Empty> result = testKit.increaseWithSideEffect(CounterApi.IncreaseValue.newBuilder().setValue(increase).build());
    assertTrue(result.didEmitEvents());
    //FIXME assertEquals(1 + 2, testKit.getState().getValue());
    DeferredCallDetails<?, ?> sideEffect = result.getSideEffects().get(0);// <2>
    assertEquals("com.example.CounterService", sideEffect.getServiceName());// <3>
    assertEquals("Increase", sideEffect.getMethodName());
    CounterApi.IncreaseValue doubledIncreased =  CounterApi.IncreaseValue.newBuilder()
        .setValue(increase*2)
        .build();
    assertEquals(doubledIncreased, sideEffect.getMessage());

  }

  @Test
  public void increaseTestWithNegativeValue() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    EventSourcedResult<Empty> result = testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(-1).build());
    assertFalse(result.didEmitEvents());
    assertTrue(result.isError());
    assertEquals("Value must be a zero or a positive number", result.getError());
  }

  @Test
  public void decreaseTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(10).build());
    EventSourcedResult<Empty> result = testKit.decrease(CounterApi.DecreaseValue.newBuilder().setValue(-1).build());
    assertTrue(result.didEmitEvents());
    assertEquals(9, testKit.getState().getValue());
  }

  @Test
  public void decreaseToMuchTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(2).build());
    EventSourcedResult<Empty> result = testKit.decrease(CounterApi.DecreaseValue.newBuilder().setValue(-3).build());
    assertTrue(result.isError());
    assertEquals("Decrease value is too high. Counter cannot become negative", result.getError());
  }


  @Test
  public void decreaseTestWithPositiveValue() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(1).build());
    EventSourcedResult<Empty> result = testKit.decrease(CounterApi.DecreaseValue.newBuilder().setValue(1).build());
    assertTrue(result.isError());
    assertEquals("Value must be a zero or a negative number", result.getError());
  }

  @Test
  public void resetTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(10).build());
    assertEquals(10, testKit.getState().getValue());
    EventSourcedResult<Empty> result = testKit.reset(CounterApi.ResetValue.getDefaultInstance());
    assertTrue(result.didEmitEvents());
    assertEquals(0, testKit.getState().getValue());
  }


  @Test
  public void getCurrentCounterTest() {
    CounterTestKit testKit = CounterTestKit.of(Counter::new);
    testKit.increase(CounterApi.IncreaseValue.newBuilder().setValue(10).build());
    assertEquals(10, testKit.getState().getValue());

    EventSourcedResult<CounterApi.CurrentCounter> result =
        testKit.getCurrentCounter(CounterApi.GetCounter.newBuilder().build());
    assertFalse(result.didEmitEvents());

    assertEquals(10, result.getReply().getValue());
  }

}