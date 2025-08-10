package io.github.dispatch4j.standalone;

import static org.assertj.core.api.Assertions.*;

import io.github.dispatch4j.core.Dispatch4j;
import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.exception.HandlerNotFoundException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Dispatch4jFactoryTest {

  private static final Logger log = LoggerFactory.getLogger(Dispatch4jFactoryTest.class);

  @Nested
  class BasicFactoryCreation {

    @Test
    void shouldCreateDispatcherWithoutExecutor() {
      // When
      var dispatcher = Dispatch4jFactory.create("io.github.dispatch4j.standalone");

      // Then
      assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldCreateDispatcherWithCustomExecutor() {
      // Given
      var executor = ForkJoinPool.commonPool();

      // When
      var dispatcher = Dispatch4jFactory.create(executor, "io.github.dispatch4j.standalone");

      // Then
      assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldCreateDispatcherWithMultiplePackages() {
      // When
      var dispatcher = Dispatch4jFactory.create("io.github.dispatch4j.standalone", "com.example");

      // Then
      assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldCreateDispatcherWithEmptyPackages() {
      // When
      var dispatcher = Dispatch4jFactory.create();

      // Then
      assertThat(dispatcher).isNotNull();
    }
  }

  @Nested
  class HandlerDiscovery {

    private Dispatch4j dispatcher;

    @BeforeEach
    void setUp() {
      dispatcher = Dispatch4jFactory.create("io.github.dispatch4j.standalone");
    }

    @Test
    void shouldDiscoverAndRegisterCommandHandlers() {
      // Given
      var command = new TestCommand("test-value");

      // When
      var result = dispatcher.send(command);

      // Then
      assertThat(result).isEqualTo("Command handled: test-value");
    }

    @Test
    void shouldDiscoverAndRegisterQueryHandlers() {
      // Given
      var query = new TestQuery(42);

      // When
      var result = dispatcher.send(query);

      // Then
      assertThat(result).isEqualTo(84);
    }

    @Test
    void shouldDiscoverAndRegisterEventHandlers() {
      // Given
      var event = new TestEvent("test-message");

      // When & Then - Should not throw
      assertThatCode(() -> dispatcher.publish(event)).doesNotThrowAnyException();
    }

    @Test
    void shouldDiscoverMultipleHandlersInSameClass() {
      // Given
      var command = new AnotherCommand(100);
      var query = new AnotherQuery("search-term");
      var event = new AnotherEvent("event-data");

      // When
      var commandResult = dispatcher.send(command);
      var queryResult = dispatcher.send(query);

      // Then
      assertThat(commandResult).isEqualTo(200);
      assertThat(queryResult).isEqualTo("Found: search-term");

      // Event should not throw
      assertThatCode(() -> dispatcher.publish(event)).doesNotThrowAnyException();
    }
  }

  @Nested
  class AsyncOperations {

    private Dispatch4j dispatcher;

    @BeforeEach
    void setUp() {
      dispatcher =
          Dispatch4jFactory.create(ForkJoinPool.commonPool(), "io.github.dispatch4j.standalone");
    }

    @Test
    void shouldHandleAsyncCommands() throws ExecutionException, InterruptedException {
      // Given
      var command = new TestCommand("async-test");

      // When
      CompletableFuture<String> future = dispatcher.sendAsync(command);
      String result = future.get();

      // Then
      assertThat(result).isEqualTo("Command handled: async-test");
    }

    @Test
    void shouldHandleAsyncQueries() throws ExecutionException, InterruptedException {
      // Given
      var query = new TestQuery(25);

      // When
      CompletableFuture<Integer> future = dispatcher.sendAsync(query);
      Integer result = future.get();

      // Then
      assertThat(result).isEqualTo(50);
    }

    @Test
    void shouldHandleAsyncEvents() throws ExecutionException, InterruptedException {
      // Given
      var event = new TestEvent("async-event");

      // When
      CompletableFuture<Void> future = dispatcher.publishAsync(event);

      // Then
      assertThatCode(() -> future.get()).doesNotThrowAnyException();
    }
  }

  @Nested
  class ErrorHandling {

    private Dispatch4j dispatcher;

    @BeforeEach
    void setUp() {
      dispatcher = Dispatch4jFactory.create("io.github.dispatch4j.standalone");
    }

    @Test
    void shouldThrowHandlerNotFoundForUnregisteredCommand() {
      // Given
      var unregisteredCommand = new UnregisteredCommand("test");

      // When & Then
      assertThatThrownBy(() -> dispatcher.send(unregisteredCommand))
          .isInstanceOf(HandlerNotFoundException.class)
          .hasMessageContaining(UnregisteredCommand.class.getName());
    }

    @Test
    void shouldThrowHandlerNotFoundForUnregisteredQuery() {
      // Given
      var unregisteredQuery = new UnregisteredQuery(123);

      // When & Then
      assertThatThrownBy(() -> dispatcher.send(unregisteredQuery))
          .isInstanceOf(HandlerNotFoundException.class)
          .hasMessageContaining(UnregisteredQuery.class.getName());
    }

    @Test
    void shouldSilentlyIgnoreUnregisteredEvents() {
      // Given
      var unregisteredEvent = new UnregisteredEvent("ignored");

      // When & Then - Should not throw
      assertThatCode(() -> dispatcher.publish(unregisteredEvent)).doesNotThrowAnyException();
    }

    @Test
    void shouldHandleRuntimeExceptionFromHandler() {
      // Given
      var command = new ErrorCommand("runtime-error");

      // When & Then
      assertThatThrownBy(() -> dispatcher.send(command))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Simulated runtime error");
    }
  }

  @Nested
  class PackageScanning {

    @Test
    void shouldOnlyScanSpecifiedPackages() {
      // Given - Create dispatcher that only scans a non-existent package
      var dispatcher = Dispatch4jFactory.create("com.nonexistent.package");

      // When & Then - Should not find handlers from our test package
      assertThatThrownBy(() -> dispatcher.send(new TestCommand("test")))
          .isInstanceOf(HandlerNotFoundException.class);
    }

    @Test
    void shouldScanMultiplePackagesCorrectly() {
      // Given
      var dispatcher =
          Dispatch4jFactory.create("io.github.dispatch4j.standalone", "com.nonexistent.package");

      // When & Then - Should still find handlers from our test package
      String result = dispatcher.send(new TestCommand("multi-package"));
      assertThat(result).isEqualTo("Command handled: multi-package");
    }
  }

  // Test message types
  @Command
  public record TestCommand(String value) {}

  @Query
  public record TestQuery(int number) {}

  @Event
  public record TestEvent(String message) {}

  @Command
  public record AnotherCommand(int value) {}

  @Query
  public record AnotherQuery(String searchTerm) {}

  @Event
  public record AnotherEvent(String data) {}

  @Command
  public record ErrorCommand(String trigger) {}

  @Command
  public record UnregisteredCommand(String value) {}

  @Query
  public record UnregisteredQuery(int number) {}

  @Event
  public record UnregisteredEvent(String message) {}

  @Nested
  @Disabled
  class InterfaceBasedHandlers {

    private Dispatch4j dispatcher;

    @BeforeEach
    void setUp() {
      dispatcher = Dispatch4jFactory.create("io.github.dispatch4j.standalone");
    }

    @Test
    void shouldDiscoverAndRegisterInterfaceBasedCommandHandlers() {
      // Given
      var command = new InterfaceCommand("interface-test");

      // When
      String result = dispatcher.send(command);

      // Then
      assertThat(result).isEqualTo("Interface command: interface-test");
    }

    @Test
    void shouldDiscoverAndRegisterInterfaceBasedQueryHandlers() {
      // Given
      var query = new InterfaceQuery("interface-search");

      // When
      String result = dispatcher.send(query);

      // Then
      assertThat(result).isEqualTo("Interface query: interface-search");
    }

    @Test
    void shouldDiscoverAndRegisterInterfaceBasedEventHandlers() {
      // Given
      var event = new InterfaceEvent("interface-event");

      // When & Then - Should not throw
      assertThatCode(() -> dispatcher.publish(event)).doesNotThrowAnyException();
    }
  }

  // Test message types for interface-based handlers
  @Command
  public record InterfaceCommand(String value) {}

  @Query
  public record InterfaceQuery(String searchTerm) {}

  @Event
  public record InterfaceEvent(String data) {}

  // Interface-based handler classes
  public static class InterfaceCommandHandlerImpl
      implements io.github.dispatch4j.core.handler.CommandHandler<InterfaceCommand, String> {
    @Override
    public String handle(InterfaceCommand command) {
      return "Interface command: " + command.value();
    }
  }

  public static class InterfaceQueryHandlerImpl
      implements io.github.dispatch4j.core.handler.QueryHandler<InterfaceQuery, String> {
    @Override
    public String handle(InterfaceQuery query) {
      return "Interface query: " + query.searchTerm();
    }
  }

  public static class InterfaceEventHandlerImpl
      implements io.github.dispatch4j.core.handler.EventHandler<InterfaceEvent> {
    @Override
    public void handle(InterfaceEvent event) {
      log.info("Interface event handled: {}", event.data());
    }
  }

  // Test handler classes (annotation-based)
  public static class TestHandlers {

    @io.github.dispatch4j.core.annotation.CommandHandler
    public String handleTestCommand(TestCommand command) {
      return "Command handled: " + command.value();
    }

    @io.github.dispatch4j.core.annotation.QueryHandler
    public Integer handleTestQuery(TestQuery query) {
      return query.number() * 2;
    }

    @io.github.dispatch4j.core.annotation.EventHandler
    public void handleTestEvent(TestEvent event) {
      log.info("Event handled: {}", event.message());
    }

    @io.github.dispatch4j.core.annotation.CommandHandler
    public String handleErrorCommand(ErrorCommand command) {
      throw new RuntimeException("Simulated runtime error");
    }
  }

  public static class MultiHandlers {

    @io.github.dispatch4j.core.annotation.CommandHandler
    public Integer handleAnotherCommand(AnotherCommand command) {
      return command.value() * 2;
    }

    @io.github.dispatch4j.core.annotation.QueryHandler
    public String handleAnotherQuery(AnotherQuery query) {
      return "Found: " + query.searchTerm();
    }

    @io.github.dispatch4j.core.annotation.EventHandler
    public void handleAnotherEvent(AnotherEvent event) {
      log.info("Another event handled: {}", event.data());
    }
  }
}
