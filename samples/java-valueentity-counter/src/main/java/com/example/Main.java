/* This code was generated by Kalix tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */

package com.example;

import kalix.javasdk.Kalix;
import com.example.actions.DoubleCounterAction;
import com.example.actions.CounterStateSubscriptionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.domain.Counter;


// tag::registration[]
// tag::registration-value-entity[]
public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);
  public static Kalix createKalix() {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `new Kalix()` instance.
    // end::registration-value-entity[]
    // end::registration[]
    return KalixFactory.withComponents(
            Counter::new,
            CounterStateSubscriptionAction::new,
            DoubleCounterAction::new);

    /* the comment hack bellow is needed to only show the Counter::new and DoubleCounterAction
    // tag::registration[]
    return KalixFactory.withComponents(
            Counter::new,
            DoubleCounterAction::new);
    // end::registration[]
     */

    /* the comment hack bellow is needed to only show the Counter::new
    // tag::registration-value-entity[]
    return KalixFactory.withComponents(
            Counter::new);
    // end::registration-value-entity[]
     */
    // tag::registration-value-entity[]
    // tag::registration[]
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Kalix service");
    createKalix().start();
  }
}
// end::registration-value-entity[]
// end::registration[]
