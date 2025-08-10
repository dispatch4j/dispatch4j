package io.github.dispatch4j.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.dispatch4j.core.Dispatcher;
import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.CommandHandler;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.EventHandler;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.annotation.QueryHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = Dispatch4jSpringBootIntegrationTest.TestConfiguration.class)
@TestPropertySource(
    properties = {
      "dispatch4j.enabled=true",
      "dispatch4j.async.core-pool-size=2",
      "dispatch4j.async.max-pool-size=5"
    })
class Dispatch4jSpringBootIntegrationTest {

  @Autowired private Dispatcher dispatcher;

  @Autowired private TestHandlers testHandlers;

  @Test
  void shouldInjectDispatcher() {
    assertThat(dispatcher).isNotNull();
  }

  @Test
  void shouldHandleCommandSynchronously() {
    // Given
    var command = new CreateUserCommand("john.doe", "john@example.com");

    // When
    String result = dispatcher.send(command);

    // Then
    assertThat(result).isEqualTo("User created: john.doe");
    assertThat(testHandlers.handledCommands).contains(command);
  }

  @Test
  void shouldHandleCommandAsynchronously() throws Exception {
    // Given
    var command = new CreateUserCommand("jane.doe", "jane@example.com");

    // When
    CompletableFuture<String> future = dispatcher.sendAsync(command);
    String result = future.get(5, TimeUnit.SECONDS);

    // Then
    assertThat(result).isEqualTo("User created: jane.doe");
    assertThat(testHandlers.handledCommands).contains(command);
  }

  @Test
  void shouldHandleQuerySynchronously() {
    // Given
    var query = new GetUserQuery("test.user");

    // When
    UserView result = dispatcher.send(query);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("test.user");
    assertThat(result.email()).isEqualTo("test.user@example.com");
    assertThat(testHandlers.handledQueries).contains(query);
  }

  @Test
  void shouldHandleQueryAsynchronously() throws Exception {
    // Given
    var query = new GetUserQuery("async.user");

    // When
    CompletableFuture<UserView> future = dispatcher.sendAsync(query);
    UserView result = future.get(5, TimeUnit.SECONDS);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("async.user");
    assertThat(result.email()).isEqualTo("async.user@example.com");
    assertThat(testHandlers.handledQueries).contains(query);
  }

  @Test
  void shouldPublishEventSynchronously() {
    // Given
    var event = new UserCreatedEvent("published.user", "published@example.com");
    testHandlers.handledEvents.clear(); // Clear any previous events

    // When
    dispatcher.publish(event);

    // Then
    assertThat(testHandlers.handledEvents).hasSize(1);
    assertThat(testHandlers.handledEvents.get(0)).isEqualTo(event);
  }

  @Test
  void shouldPublishEventAsynchronously() {
    // Given
    var event = new UserCreatedEvent("async.published", "async@example.com");
    testHandlers.handledEvents.clear(); // Clear any previous events

    // When
    CompletableFuture<Void> future = dispatcher.publishAsync(event);

    // Then
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(testHandlers.handledEvents).contains(event));

    assertThat(future).succeedsWithin(5, TimeUnit.SECONDS);
  }

  @Test
  void shouldHandleMultipleHandlersInSingleClass() {
    // Given
    var command = new CreateUserCommand("multi.user", "multi@example.com");
    var query = new GetUserQuery("multi.user");
    var event = new UserCreatedEvent("multi.user", "multi@example.com");

    testHandlers.handledCommands.clear();
    testHandlers.handledQueries.clear();
    testHandlers.handledEvents.clear();

    // When
    String commandResult = dispatcher.send(command);
    UserView queryResult = dispatcher.send(query);
    dispatcher.publish(event);

    // Then
    assertThat(commandResult).isEqualTo("User created: multi.user");
    assertThat(queryResult.username()).isEqualTo("multi.user");
    assertThat(testHandlers.handledCommands).hasSize(1);
    assertThat(testHandlers.handledQueries).hasSize(1);
    assertThat(testHandlers.handledEvents).hasSize(1);
  }

  // Test domain objects

  @Command
  record CreateUserCommand(String username, String email) {}

  @Query
  record GetUserQuery(String username) {}

  @Event
  record UserCreatedEvent(String username, String email) {}

  record UserView(String username, String email) {}

  // Test configuration

  @Configuration
  @EnableAutoConfiguration
  static class TestConfiguration {

    @Bean
    public TestHandlers testHandlers() {
      return new TestHandlers();
    }
  }

  // Test handlers

  static class TestHandlers {
    final List<CreateUserCommand> handledCommands = new ArrayList<>();
    final List<GetUserQuery> handledQueries = new ArrayList<>();
    final List<UserCreatedEvent> handledEvents = new ArrayList<>();

    @CommandHandler
    public String handle(CreateUserCommand command) {
      handledCommands.add(command);
      return "User created: " + command.username();
    }

    @QueryHandler
    public UserView handle(GetUserQuery query) {
      handledQueries.add(query);
      return new UserView(query.username(), query.username() + "@example.com");
    }

    @EventHandler
    public void handle(UserCreatedEvent event) {
      handledEvents.add(event);
    }
  }
}
