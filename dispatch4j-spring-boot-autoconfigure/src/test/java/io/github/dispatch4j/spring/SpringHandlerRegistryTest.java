package io.github.dispatch4j.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.CommandHandler;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.EventHandler;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.annotation.QueryHandler;
import io.github.dispatch4j.discovery.CompositeDiscoveryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;

class SpringHandlerRegistryTest {

    private SpringHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SpringHandlerRegistry(CompositeDiscoveryStrategy.createDefault());
    }

    @Test
    void shouldRegisterCommandHandlerFromAnnotatedMethod() {
        // Given
        var handler = new TestCommandHandler();

        // When
        registry.postProcessAfterInitialization(handler, "testHandler");

        // Then
        var commandHandler = registry.getCommandHandler(TestCommand.class);
        assertThat(commandHandler).isNotNull();

        var result = commandHandler.handle(new TestCommand("test"));
        assertThat(result).isEqualTo("Command: test");
    }

    @Test
    void shouldRegisterQueryHandlerFromAnnotatedMethod() {
        // Given
        var handler = new TestQueryHandler();

        // When
        registry.postProcessAfterInitialization(handler, "testHandler");

        // Then
        var queryHandler = registry.getQueryHandler(TestQuery.class);
        assertThat(queryHandler).isNotNull();

        var result = queryHandler.handle(new TestQuery("test"));
        assertThat(result).isEqualTo("Query: test");
    }

    @Test
    void shouldRegisterEventHandlerFromAnnotatedMethod() {
        // Given
        var handler = new TestEventHandler();

        // When
        registry.postProcessAfterInitialization(handler, "testHandler");

        // Then
        var eventHandlers = registry.getEventHandlers(TestEvent.class);
        assertThat(eventHandlers).hasSize(1);

        // Verify handler execution
        @SuppressWarnings("unchecked")
        var eventHandler =
                (io.github.dispatch4j.handler.EventHandler<TestEvent>) eventHandlers.get(0);
        eventHandler.handle(new TestEvent("test"));
        assertThat(handler.handledEvent).isEqualTo("test");
    }

    @Test
    void shouldRegisterMultipleHandlersFromSingleClass() {
        // Given
        var handler = new MultipleHandlerClass();

        // When
        registry.postProcessAfterInitialization(handler, "multiHandler");

        // Then
        assertThat(registry.getCommandHandler(TestCommand.class)).isNotNull();
        assertThat(registry.getQueryHandler(TestQuery.class)).isNotNull();
        assertThat(registry.getEventHandlers(TestEvent.class)).hasSize(1);
    }

    @Test
    void shouldValidateCommandHandlerMethodParameters() {
        // Given
        var handler = new InvalidCommandHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateCommandHandlerParameterAnnotation() {
        // Given
        var handler = new InvalidCommandParameterHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateCommandHandlerReturnType() {
        // Given
        var handler = new VoidCommandHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "voidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: voidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateQueryHandlerMethodParameters() {
        // Given
        var handler = new InvalidQueryHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateQueryHandlerParameterAnnotation() {
        // Given
        var handler = new InvalidQueryParameterHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateQueryHandlerReturnType() {
        // Given
        var handler = new VoidQueryHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "voidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: voidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateEventHandlerMethodParameters() {
        // Given
        var handler = new InvalidEventHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateEventHandlerParameterAnnotation() {
        // Given
        var handler = new InvalidEventParameterHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "invalidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: invalidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldValidateEventHandlerReturnType() {
        // Given
        var handler = new NonVoidEventHandler();

        // When & Then
        assertThatThrownBy(() -> registry.postProcessAfterInitialization(handler, "nonVoidHandler"))
                .isInstanceOf(BeansException.class)
                .hasMessageContaining("Handler discovery failed for bean: nonVoidHandler")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void shouldIgnoreClassesWithoutAnnotatedMethods() {
        // Given
        var handler = new NonHandlerClass();

        // When
        var result = registry.postProcessAfterInitialization(handler, "nonHandler");

        // Then
        assertThat(result).isSameAs(handler);
        assertThat(registry.getCommandHandler(String.class)).isNull();
    }

    @Test
    void shouldWorkForLambdas() {
        // Given
        var handler = new NonHandlerClass();

        // When
        var result = registry.postProcessAfterInitialization(handler, "nonHandler");

        // Then
        assertThat(result).isSameAs(handler);
        assertThat(registry.getCommandHandler(String.class)).isNull();
    }

    // Test classes and records

    @Command
    record TestCommand(String value) {}

    @Query
    record TestQuery(String value) {}

    @Event
    record TestEvent(String value) {}

    static class TestCommandHandler {
        @CommandHandler
        public String handle(TestCommand command) {
            return "Command: " + command.value();
        }
    }

    static class TestQueryHandler {
        @QueryHandler
        public String handle(TestQuery query) {
            return "Query: " + query.value();
        }
    }

    static class TestEventHandler {
        String handledEvent;

        @EventHandler
        public void handle(TestEvent event) {
            this.handledEvent = event.value();
        }
    }

    static class MultipleHandlerClass {
        @CommandHandler
        public String handleCommand(TestCommand command) {
            return "Command: " + command.value();
        }

        @QueryHandler
        public String handleQuery(TestQuery query) {
            return "Query: " + query.value();
        }

        @EventHandler
        public void handleEvent(TestEvent event) {
            // Handle event
        }
    }

    static class InvalidCommandHandler {
        @CommandHandler
        public String handleCommand() {
            return "invalid";
        }
    }

    static class InvalidCommandParameterHandler {
        @CommandHandler
        public String handleCommand(String notCommand) {
            return "invalid";
        }
    }

    static class VoidCommandHandler {
        @CommandHandler
        public void handleCommand(TestCommand command) {
            // void return
        }
    }

    static class InvalidQueryHandler {
        @QueryHandler
        public String handleQuery(TestQuery query, String extra) {
            return "invalid";
        }
    }

    static class InvalidQueryParameterHandler {
        @QueryHandler
        public String handleQuery(String notQuery) {
            return "invalid";
        }
    }

    static class VoidQueryHandler {
        @QueryHandler
        public void handleQuery(TestQuery query) {
            // void return
        }
    }

    static class InvalidEventHandler {
        @EventHandler
        public void handleEvent() {
            // no parameters
        }
    }

    static class InvalidEventParameterHandler {
        @EventHandler
        public void handleEvent(String notEvent) {
            // wrong parameter type
        }
    }

    static class NonVoidEventHandler {
        @EventHandler
        public String handleEvent(TestEvent event) {
            return "should be void";
        }
    }

    static class NonHandlerClass {
        public void someMethod() {
            // no handler annotations
        }
    }
}
