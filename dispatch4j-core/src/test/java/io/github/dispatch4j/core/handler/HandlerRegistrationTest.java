package io.github.dispatch4j.core.handler;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
class HandlerRegistrationTest {

  private static final Logger log = LoggerFactory.getLogger(HandlerRegistrationTest.class);

  private List<RegisteredHandler> registeredCommandHandlers;
  private List<RegisteredHandler> registeredQueryHandlers;
  private List<RegisteredHandler> registeredEventHandlers;

  @BeforeEach
  void setUp() {
    registeredCommandHandlers = new ArrayList<>();
    registeredQueryHandlers = new ArrayList<>();
    registeredEventHandlers = new ArrayList<>();
  }

  private void registerHandlers(Object... handlers) {
    Arrays.stream(handlers)
        .forEach(
            handler ->
                HandlerRegistration.registerInterfaceBasedHandler(
                    handler,
                    (type, h) -> registeredCommandHandlers.add(new RegisteredHandler(type, h)),
                    (type, h) -> registeredQueryHandlers.add(new RegisteredHandler(type, h)),
                    (type, h) -> registeredEventHandlers.add(new RegisteredHandler(type, h))));
  }

  @Test
  void shouldRegisterCommandHandlerInterface() {
    // Given
    var handler = new TestCommandHandlerImpl();

    // When
    registerHandlers(handler);

    // Then
    assertHandlerRegistered(registeredCommandHandlers, TestCommand.class, handler);
    assertThat(registeredQueryHandlers).isEmpty();
    assertThat(registeredEventHandlers).isEmpty();
  }

  @Test
  void shouldRegisterQueryHandlerInterface() {
    // Given
    var handler = new TestQueryHandlerImpl();

    // When
    registerHandlers(handler);

    // Then
    assertHandlerRegistered(registeredQueryHandlers, TestQuery.class, handler);
    assertThat(registeredCommandHandlers).isEmpty();
    assertThat(registeredEventHandlers).isEmpty();
  }

  @Test
  void shouldRegisterEventHandlerInterface() {
    // Given
    var handler = new TestEventHandlerImpl();

    // When
    registerHandlers(handler);

    // Then
    assertHandlerRegistered(registeredEventHandlers, TestEvent.class, handler);
    assertThat(registeredCommandHandlers).isEmpty();
    assertThat(registeredQueryHandlers).isEmpty();
  }

  @Test
  void shouldRegisterMultipleHandlersFromDifferentClasses() {
    // Given
    var commandHandler = new TestCommandHandlerImpl();
    var queryHandler = new TestQueryHandlerImpl();
    var eventHandler = new TestEventHandlerImpl();

    // When - Register all handlers
    registerHandlers(commandHandler, queryHandler, eventHandler);

    // Then
    assertHandlerRegistered(registeredCommandHandlers, TestCommand.class, commandHandler);
    assertHandlerRegistered(registeredQueryHandlers, TestQuery.class, queryHandler);
    assertHandlerRegistered(registeredEventHandlers, TestEvent.class, eventHandler);
  }

  @Test
  void shouldIgnoreNonHandlerInterfaces() {
    // Given
    var handler = new NonHandlerInterfaceImpl();

    // When
    registerHandlers(handler);

    // Then
    assertThat(registeredCommandHandlers).isEmpty();
    assertThat(registeredQueryHandlers).isEmpty();
    assertThat(registeredEventHandlers).isEmpty();
  }

  @Test
  void shouldIgnoreUnannotatedMessageTypes() {
    // Given
    var handler = new UnannotatedMessageHandlerImpl();

    // When
    registerHandlers(handler);

    // Then - Should not register handlers for unannotated message types
    assertThat(registeredCommandHandlers).isEmpty();
    assertThat(registeredQueryHandlers).isEmpty();
    assertThat(registeredEventHandlers).isEmpty();
  }

  @Test
  void shouldHandleLambdaRegistration() {
    var commandHandler =
        (CommandHandler<TestCommand, String>) command -> "Lambda handled: " + command.value();
    var queryHandler =
        (QueryHandler<TestQuery, String>) query -> "Lambda query: " + query.searchTerm();
    var eventHandler =
        (EventHandler<TestEvent>) event -> log.info("Lambda event: {}", event.data());

    // when
    registerHandlers(commandHandler, queryHandler, eventHandler);

    // then
    assertHandlerRegistered(registeredCommandHandlers, TestCommand.class, commandHandler);
    assertHandlerRegistered(registeredQueryHandlers, TestQuery.class, queryHandler);
    assertHandlerRegistered(registeredEventHandlers, TestEvent.class, eventHandler);
  }

  private void assertHandlerRegistered(
      List<RegisteredHandler> handlers, Class<?> type, Object handler) {
    assertThat(handlers)
        .hasSize(1)
        .first()
        .extracting(RegisteredHandler::type, RegisteredHandler::handler)
        .containsExactly(type, handler);
  }

  // Test message types
  @Command
  record TestCommand(String value) {}

  @Query
  record TestQuery(String searchTerm) {}

  @Event
  record TestEvent(String data) {}

  // Unannotated message type for negative testing
  record UnannotatedMessage(String data) {}

  // Test handler implementations
  static class TestCommandHandlerImpl implements CommandHandler<TestCommand, String> {
    @Override
    public String handle(TestCommand command) {
      return "Handled: " + command.value();
    }
  }

  static class TestQueryHandlerImpl implements QueryHandler<TestQuery, String> {
    @Override
    public String handle(TestQuery query) {
      return "Query: " + query.searchTerm();
    }
  }

  static class TestEventHandlerImpl implements EventHandler<TestEvent> {
    @Override
    public void handle(TestEvent event) {
      log.info("Event: {}", event.data());
    }
  }

  static class NonHandlerInterfaceImpl implements Runnable {
    @Override
    public void run() {
      // This should be ignored
    }
  }

  static class UnannotatedMessageHandlerImpl implements CommandHandler<UnannotatedMessage, String> {
    @Override
    public String handle(UnannotatedMessage command) {
      return "Should not be registered: " + command.data();
    }
  }

  // Helper record for testing
  record RegisteredHandler(Class<?> type, Object handler) {}
}
