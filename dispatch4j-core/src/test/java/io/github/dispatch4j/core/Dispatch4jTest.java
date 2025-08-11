package io.github.dispatch4j.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.exception.Dispatch4jException;
import io.github.dispatch4j.core.exception.HandlerNotFoundException;
import io.github.dispatch4j.core.exception.MultipleHandlersFoundException;
import io.github.dispatch4j.core.handler.CommandHandler;
import io.github.dispatch4j.core.handler.EventHandler;
import io.github.dispatch4j.core.handler.QueryHandler;
import io.github.dispatch4j.core.middleware.HandlerMiddleware;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class Dispatch4jTest {

    private Dispatch4j dispatcher;

    @Mock private CommandHandler<TestCommand, String> commandHandler;

    @Mock private QueryHandler<TestQuery, Integer> queryHandler;

    @Mock private EventHandler<TestEvent> eventHandler;

    @Mock private HandlerMiddleware middleware1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dispatcher = new Dispatch4j();
    }

    @Test
    void shouldHandleCommand() {
        // Given
        var command = new TestCommand("test");
        when(commandHandler.handle(command)).thenReturn("Result: test");
        dispatcher.registerCommandHandler(TestCommand.class, commandHandler);

        // When
        String result = dispatcher.send(command);

        // Then
        assertThat(result).isEqualTo("Result: test");
        verify(commandHandler).handle(command);
    }

    @Test
    void shouldHandleQuery() {
        // Given
        var query = new TestQuery(5);
        when(queryHandler.handle(query)).thenReturn(10);
        dispatcher.registerQueryHandler(TestQuery.class, queryHandler);

        // When
        Integer result = dispatcher.send(query);

        // Then
        assertThat(result).isEqualTo(10);
        verify(queryHandler).handle(query);
    }

    @Test
    void shouldHandleEvent() {
        // Given
        var event = new TestEvent("test event");
        dispatcher.registerEventHandler(TestEvent.class, eventHandler);

        // When
        dispatcher.publish(event);

        // Then
        verify(eventHandler).handle(event);
    }

    @Test
    void shouldHandleAsyncCommand() throws ExecutionException, InterruptedException {
        // Given
        var command = new TestCommand("async");
        when(commandHandler.handle(command)).thenReturn("Async: async");
        dispatcher.registerCommandHandler(TestCommand.class, commandHandler);

        // When
        CompletableFuture<String> future = dispatcher.sendAsync(command);

        // Then
        assertThat(future.get()).isEqualTo("Async: async");
        verify(commandHandler).handle(command);
    }

    @Test
    void shouldThrowExceptionWhenHandlerNotFound() {
        // When & Then
        assertThatThrownBy(() -> dispatcher.send(new TestCommand("no handler")))
                .isInstanceOf(HandlerNotFoundException.class)
                .hasMessageContaining("TestCommand");
    }

    @Test
    void shouldPropagateRuntimeExceptionFromCommandHandler() {
        // Given
        CommandHandler<FailingCommand, String> failingHandler =
                cmd -> {
                    throw new RuntimeException("Command handler failed: " + cmd.message());
                };
        dispatcher.registerCommandHandler(FailingCommand.class, failingHandler);

        var command = new FailingCommand("test error");

        // When & Then
        assertThatThrownBy(() -> dispatcher.send(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Command handler failed: test error");
    }

    @Test
    void shouldPropagateRuntimeExceptionFromQueryHandler() {
        // Given
        QueryHandler<FailingQuery, String> failingHandler =
                query -> {
                    throw new IllegalStateException("Query handler failed: " + query.message());
                };
        dispatcher.registerQueryHandler(FailingQuery.class, failingHandler);

        FailingQuery query = new FailingQuery("test error");

        // When & Then
        assertThatThrownBy(() -> dispatcher.send(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Query handler failed: test error");
    }

    @Test
    void shouldPropagateRuntimeExceptionFromEventHandler() {
        // Given
        EventHandler<FailingEvent> failingHandler =
                event -> {
                    throw new RuntimeException("Event handler failed: " + event.message());
                };
        dispatcher.registerEventHandler(FailingEvent.class, failingHandler);

        var event = new FailingEvent("test error");

        // When & Then
        assertThatThrownBy(() -> dispatcher.publish(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event handler failed: test error");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenMultipleHandlersRegistered() {
        // Given
        CommandHandler<TestCommand, String> handler1 = mock(CommandHandler.class);
        CommandHandler<TestCommand, String> handler2 = mock(CommandHandler.class);

        dispatcher.registerCommandHandler(TestCommand.class, handler1);

        // When & Then
        assertThatThrownBy(() -> dispatcher.registerCommandHandler(TestCommand.class, handler2))
                .isInstanceOf(MultipleHandlersFoundException.class)
                .hasMessageContaining("TestCommand");
    }

    @Test
    void shouldThrowMultipleHandlersExceptionForQueryHandlers() {
        // Given
        QueryHandler<TestQuery, String> handler1 = query -> "handler1";
        QueryHandler<TestQuery, String> handler2 = query -> "handler2";

        dispatcher.registerQueryHandler(TestQuery.class, handler1);

        // When & Then
        assertThatThrownBy(() -> dispatcher.registerQueryHandler(TestQuery.class, handler2))
                .isInstanceOf(MultipleHandlersFoundException.class)
                .hasMessageContaining("io.github.dispatch4j.core.Dispatch4jTest$TestQuery");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleMultipleEventHandlers() {
        // Given
        var event = new TestEvent("multi");
        EventHandler<TestEvent> handler1 = mock(EventHandler.class);
        EventHandler<TestEvent> handler2 = mock(EventHandler.class);

        dispatcher.registerEventHandler(TestEvent.class, handler1);
        dispatcher.registerEventHandler(TestEvent.class, handler2);

        // When
        dispatcher.publish(event);

        // Then
        verify(handler1).handle(event);
        verify(handler2).handle(event);
    }

    @Test
    void shouldVerifyHandlerExecutionOrder() {
        // Given
        var command = new TestCommand("order test");
        when(commandHandler.handle(command)).thenReturn("executed");
        dispatcher.registerCommandHandler(TestCommand.class, commandHandler);

        // When
        dispatcher.send(command);

        // Then
        verify(commandHandler, times(1)).handle(command);
        verifyNoMoreInteractions(commandHandler);
    }

    @Test
    void shouldVerifyAsyncEventHandlerExecution() throws ExecutionException, InterruptedException {
        // Given
        var event = new TestEvent("async event");
        dispatcher.registerEventHandler(TestEvent.class, eventHandler);

        // When
        CompletableFuture<Void> future = dispatcher.publishAsync(event);
        future.get(); // Wait for completion

        // Then
        verify(eventHandler).handle(event);
    }

    @Test
    void shouldMutateMiddlewareChain() {
        // Given
        var command = new TestCommand("middleware test");
        when(commandHandler.handle(command)).thenReturn("handled");
        dispatcher.registerCommandHandler(TestCommand.class, commandHandler);

        // When
        dispatcher.send(command);

        // Then
        verify(commandHandler).handle(command);
        verifyNoInteractions(middleware1);

        // When adding middleware
        dispatcher.mutateMiddleware(
                mutator -> {
                    mutator.add(middleware1);
                });
        dispatcher.send(command);

        // Then should call middleware
        verify(commandHandler).handle(command);
        verify(middleware1).handle(eq(command), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenMutatorConsumerIsNull() {
        // When & Then
        assertThatThrownBy(() -> dispatcher.mutateMiddleware(null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Mutator consumer cannot be null");
    }

    @Command
    record TestCommand(String value) {}

    @Query
    record TestQuery(int value) {}

    @Event
    record TestEvent(String message) {}

    @Command
    record FailingCommand(String message) {}

    @Query
    record FailingQuery(String message) {}

    @Event
    record FailingEvent(String message) {}
}
