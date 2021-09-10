/* This code was generated by Akka Serverless tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */
package customer.domain;

import com.akkaserverless.javasdk.testkit.junit.AkkaServerlessTestkitResource;
import com.google.protobuf.Empty;
import customer.Main;
import customer.api.CustomerApi;
import customer.api.CustomerServiceClient;
import org.junit.ClassRule;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.*;

// Example of an integration test calling our service via the Akka Serverless proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class CustomerIntegrationTest {

  /**
   * The test kit starts both the service container and the Akka Serverless proxy.
   */
  @ClassRule
  public static final AkkaServerlessTestkitResource testkit =
    new AkkaServerlessTestkitResource(Main.createAkkaServerless());

  /**
   * Use the generated gRPC client to call the service through the Akka Serverless proxy.
   */
  private final CustomerServiceClient client;

  public CustomerIntegrationTest() {
    client = CustomerServiceClient.create(testkit.getGrpcClientSettings(), testkit.getActorSystem());
  }

  @Test
  public void createOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.create(CustomerApi.Customer.newBuilder().build())
    //         .toCompletableFuture().get(2, SECONDS);
  }

  @Test
  public void getCustomerOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.getCustomer(CustomerApi.GetCustomerRequest.newBuilder().build())
    //         .toCompletableFuture().get(2, SECONDS);
  }

  @Test
  public void changeNameOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.changeName(CustomerApi.ChangeNameRequest.newBuilder().build())
    //         .toCompletableFuture().get(2, SECONDS);
  }

  @Test
  public void changeAddressOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.changeAddress(CustomerApi.ChangeAddressRequest.newBuilder().build())
    //         .toCompletableFuture().get(2, SECONDS);
  }
}