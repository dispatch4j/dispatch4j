package io.github.dispatch4j.core.handler;

import static org.assertj.core.api.Assertions.*;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.exception.Dispatch4jException;
import io.github.dispatch4j.core.exception.MultipleHandlersFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HandlerRegistryTest {

  private static final Logger log = LoggerFactory.getLogger(HandlerRegistryTest.class);
  private HandlerRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new HandlerRegistry();
  }

  @Test
  void shouldRegisterCommandHandler() {
    // Given
    CommandHandler<TestCommand, String> handler = cmd -> "handled: " + cmd.value();

    // When
    registry.registerCommandHandler(TestCommand.class, handler);

    // Then
    CommandHandler<?, ?> retrievedHandler = registry.getCommandHandler(TestCommand.class);
    assertThat(retrievedHandler).isNotNull().isSameAs(handler);
  }

  @Test
  void shouldRegisterQueryHandler() {
    // Given
    QueryHandler<TestQuery, Integer> handler = query -> query.value() * 2;

    // When
    registry.registerQueryHandler(TestQuery.class, handler);

    // Then
    QueryHandler<?, ?> retrievedHandler = registry.getQueryHandler(TestQuery.class);
    assertThat(retrievedHandler).isNotNull().isSameAs(handler);
  }

  @Test
  void shouldRegisterSingleEventHandler() {
    // Given
    EventHandler<TestEvent> handler = event -> log.info(event.message());

    // When
    registry.registerEventHandler(TestEvent.class, handler);

    // Then
    List<EventHandler<?>> handlers = registry.getEventHandlers(TestEvent.class);
    assertThat(handlers).hasSize(1).containsExactly(handler);
  }

  @Test
  void shouldRegisterMultipleEventHandlers() {
    // Given
    EventHandler<TestEvent> handler1 = event -> log.info("Handler 1: {}", event.message());
    EventHandler<TestEvent> handler2 = event -> log.info("Handler 2: {}", event.message());
    EventHandler<TestEvent> handler3 = event -> log.info("Handler 3: {}", event.message());

    // When
    registry.registerEventHandler(TestEvent.class, handler1);
    registry.registerEventHandler(TestEvent.class, handler2);
    registry.registerEventHandler(TestEvent.class, handler3);

    // Then
    List<EventHandler<?>> handlers = registry.getEventHandlers(TestEvent.class);
    assertThat(handlers).hasSize(3).containsExactly(handler1, handler2, handler3);
  }

  @Test
  void shouldThrowExceptionWhenRegisteringDuplicateCommandHandler() {
    // Given
    CommandHandler<TestCommand, String> handler1 = cmd -> "handler1";
    CommandHandler<TestCommand, String> handler2 = cmd -> "handler2";
    registry.registerCommandHandler(TestCommand.class, handler1);

    // When & Then
    assertThatThrownBy(() -> registry.registerCommandHandler(TestCommand.class, handler2))
        .isInstanceOf(MultipleHandlersFoundException.class)
        .hasMessageContaining(TestCommand.class.getName());
  }

  @Test
  void shouldThrowExceptionWhenRegisteringDuplicateQueryHandler() {
    // Given
    QueryHandler<TestQuery, Integer> handler1 = query -> 1;
    QueryHandler<TestQuery, Integer> handler2 = query -> 2;
    registry.registerQueryHandler(TestQuery.class, handler1);

    // When & Then
    assertThatThrownBy(() -> registry.registerQueryHandler(TestQuery.class, handler2))
        .isInstanceOf(MultipleHandlersFoundException.class)
        .hasMessageContaining(TestQuery.class.getName());
  }

  @Test
  void shouldAllowMultipleEventHandlersForSameType() {
    // Given
    EventHandler<TestEvent> handler1 = event -> {};
    EventHandler<TestEvent> handler2 = event -> {};

    // When & Then - Should not throw
    assertThatCode(
            () -> {
              registry.registerEventHandler(TestEvent.class, handler1);
              registry.registerEventHandler(TestEvent.class, handler2);
            })
        .doesNotThrowAnyException();

    List<EventHandler<?>> handlers = registry.getEventHandlers(TestEvent.class);
    assertThat(handlers).hasSize(2);
  }

  @Test
  void shouldReturnNullForUnregisteredCommandHandler() {
    // When
    CommandHandler<?, ?> handler = registry.getCommandHandler(TestCommand.class);

    // Then
    assertThat(handler).isNull();
  }

  @Test
  void shouldReturnNullForUnregisteredQueryHandler() {
    // When
    QueryHandler<?, ?> handler = registry.getQueryHandler(TestQuery.class);

    // Then
    assertThat(handler).isNull();
  }

  @Test
  void shouldReturnEmptyListForUnregisteredEventHandlers() {
    // When
    List<EventHandler<?>> handlers = registry.getEventHandlers(TestEvent.class);

    // Then
    assertThat(handlers).isEmpty();
  }

  @Test
  void shouldHandleDifferentMessageTypes() {
    // Given
    CommandHandler<TestCommand, String> cmdHandler = cmd -> "cmd";
    CommandHandler<AnotherCommand, Integer> anotherCmdHandler = cmd -> 42;
    QueryHandler<TestQuery, String> queryHandler = query -> "query";
    EventHandler<TestEvent> eventHandler = event -> {};

    // When
    registry.registerCommandHandler(TestCommand.class, cmdHandler);
    registry.registerCommandHandler(AnotherCommand.class, anotherCmdHandler);
    registry.registerQueryHandler(TestQuery.class, queryHandler);
    registry.registerEventHandler(TestEvent.class, eventHandler);

    // Then
    assertThat(registry.getCommandHandler(TestCommand.class)).isSameAs(cmdHandler);
    assertThat(registry.getCommandHandler(AnotherCommand.class)).isSameAs(anotherCmdHandler);
    assertThat(registry.getQueryHandler(TestQuery.class)).isSameAs(queryHandler);
    assertThat(registry.getEventHandlers(TestEvent.class)).containsExactly(eventHandler);
  }

  @Test
  void shouldMaintainEventHandlerOrder() {
    // Given
    EventHandler<TestEvent> first = event -> log.info("first");
    EventHandler<TestEvent> second = event -> log.info("second");
    EventHandler<TestEvent> third = event -> log.info("third");

    // When - Register in specific order
    registry.registerEventHandler(TestEvent.class, first);
    registry.registerEventHandler(TestEvent.class, second);
    registry.registerEventHandler(TestEvent.class, third);

    // Then - Should maintain registration order
    List<EventHandler<?>> handlers = registry.getEventHandlers(TestEvent.class);
    assertThat(handlers).containsExactly(first, second, third);
  }

  @Test
  void shouldHandleNullParameters() {
    // When & Then
    assertThatThrownBy(() -> registry.registerCommandHandler(null, cmd -> "result"))
        .isInstanceOf(Dispatch4jException.class);

    assertThatThrownBy(() -> registry.registerCommandHandler(TestCommand.class, null))
        .isInstanceOf(Dispatch4jException.class);

    assertThatThrownBy(() -> registry.getCommandHandler(null))
        .isInstanceOf(Dispatch4jException.class);
  }

  @Test
  void shouldValidateEventHandlerParameters() {
    // Given
    EventHandler<TestEvent> validHandler = event -> {};

    // When & Then
    assertThatThrownBy(() -> registry.registerEventHandler(null, validHandler))
        .isInstanceOf(Dispatch4jException.class);

    assertThatThrownBy(() -> registry.registerEventHandler(TestEvent.class, null))
        .isInstanceOf(Dispatch4jException.class);

    assertThatThrownBy(() -> registry.getEventHandlers(null))
        .isInstanceOf(Dispatch4jException.class);
  }

  @Test
  void shouldHandleComplexMessageTypes() {
    // Given
    CommandHandler<ComplexCommand, ComplexResult> handler =
        cmd -> new ComplexResult(cmd.nested().value() + "-processed", cmd.count() * 2);

    // When
    registry.registerCommandHandler(ComplexCommand.class, handler);

    // Then
    CommandHandler<?, ?> retrievedHandler = registry.getCommandHandler(ComplexCommand.class);
    assertThat(retrievedHandler).isNotNull().isSameAs(handler);
  }

  // Test message types
  @Command
  record TestCommand(String value) {}

  @Command
  record AnotherCommand(int number) {}

  @Query
  record TestQuery(int value) {}

  @Event
  record TestEvent(String message) {}

  @Command
  record ComplexCommand(NestedType nested, int count) {}

  record NestedType(String value) {}

  record ComplexResult(String processedValue, int doubledCount) {}
}
