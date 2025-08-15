package io.github.dispatch4j.handler;

import static io.github.dispatch4j.discovery.ConflictResolutionStrategy.MERGE_ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.CommandHandler;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.EventHandler;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.annotation.QueryHandler;
import io.github.dispatch4j.discovery.AnnotationBasedDiscoveryStrategy;
import io.github.dispatch4j.discovery.CompositeDiscoveryStrategy;
import io.github.dispatch4j.discovery.ConflictResolutionStrategy;
import io.github.dispatch4j.discovery.InterfaceBasedDiscoveryStrategy;
import io.github.dispatch4j.exception.Dispatch4jException;
import io.github.dispatch4j.exception.HandlerDiscoveryException;
import io.github.dispatch4j.exception.HandlerValidationException;
import io.github.dispatch4j.exception.MultipleHandlersFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HandlerRegistry Strategy Integration")
class HandlerRegistryStrategyIntegrationTest {

    private HandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = createRegistry(ConflictResolutionStrategy.FAIL_FAST);
    }

    private HandlerRegistry createRegistry(ConflictResolutionStrategy conflictResolutionStrategy) {
        return new HandlerRegistry(
                new CompositeDiscoveryStrategy(
                        List.of(
                                new AnnotationBasedDiscoveryStrategy(),
                                new InterfaceBasedDiscoveryStrategy()),
                        conflictResolutionStrategy));
    }

    @Nested
    @DisplayName("Annotation-based Discovery")
    class AnnotationBasedDiscovery {

        @Test
        @DisplayName("should discover and register annotated command handler")
        void shouldDiscoverAndRegisterAnnotatedCommandHandler() {
            var handler = new AnnotatedCommandHandler();

            registry.registerHandlersIn(handler);

            var registeredHandler = registry.getCommandHandler(TestCommand.class);
            assertThat(registeredHandler).isNotNull();

            var result = registeredHandler.handle(new TestCommand("test"));
            assertThat(result).isEqualTo("Command: test");
        }

        @Test
        @DisplayName("should discover and register annotated query handler")
        void shouldDiscoverAndRegisterAnnotatedQueryHandler() {
            var handler = new AnnotatedQueryHandler();

            registry.registerHandlersIn(handler);

            var registeredHandler = registry.getQueryHandler(TestQuery.class);
            assertThat(registeredHandler).isNotNull();

            var result = registeredHandler.handle(new TestQuery("query"));
            assertThat(result).isEqualTo("Query: query");
        }

        @Test
        @DisplayName("should discover and register annotated event handler")
        void shouldDiscoverAndRegisterAnnotatedEventHandler() {
            var handler = new AnnotatedEventHandler();

            registry.registerHandlersIn(handler);

            var handlers = registry.getEventHandlers(TestEvent.class);
            assertThat(handlers).hasSize(1);
        }

        @Test
        @DisplayName("should register multiple annotated handlers from same class")
        void shouldRegisterMultipleAnnotatedHandlersFromSameClass() {
            var handler = new MultipleAnnotatedHandlers();

            registry.registerHandlersIn(handler);

            assertThat(registry.getCommandHandler(TestCommand.class)).isNotNull();
            assertThat(registry.getQueryHandler(TestQuery.class)).isNotNull();
            assertThat(registry.getEventHandlers(TestEvent.class)).hasSize(1);
        }

        @Test
        @DisplayName("should validate annotated handler methods")
        void shouldValidateAnnotatedHandlerMethods() {
            var handler = new InvalidAnnotatedHandler();

            assertThatThrownBy(() -> registry.registerHandlersIn(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .cause()
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }
    }

    @Nested
    @DisplayName("Interface-based Discovery")
    class InterfaceBasedDiscovery {

        @Test
        @DisplayName("should discover and register CommandHandler interface")
        void shouldDiscoverAndRegisterCommandHandlerInterface() {
            var handler = new CommandHandlerImpl();

            registry.registerHandlersIn(handler);

            var registeredHandler = registry.getCommandHandler(TestCommand.class);
            assertThat(registeredHandler).isNotNull();

            var result = registeredHandler.handle(new TestCommand("test"));
            assertThat(result).isEqualTo("Interface Command: test");
        }

        @Test
        @DisplayName("should discover and register QueryHandler interface")
        void shouldDiscoverAndRegisterQueryHandlerInterface() {
            var handler = new QueryHandlerImpl();

            registry.registerHandlersIn(handler);

            var registeredHandler = registry.getQueryHandler(TestQuery.class);
            assertThat(registeredHandler).isNotNull();

            var result = registeredHandler.handle(new TestQuery("query"));
            assertThat(result).isEqualTo("Interface Query: query");
        }

        @Test
        @DisplayName("should discover and register EventHandler interface")
        void shouldDiscoverAndRegisterEventHandlerInterface() {
            var handler = new EventHandlerImpl();

            registry.registerHandlersIn(handler);

            var handlers = registry.getEventHandlers(TestEvent.class);
            assertThat(handlers).hasSize(1);
        }

        @Test
        @DisplayName("should register handler implementing single interface")
        void shouldRegisterHandlerImplementingSingleInterface() {
            var handler = new CommandHandlerImpl();

            registry.registerHandlersIn(handler);

            assertThat(registry.getCommandHandler(TestCommand.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mixed Discovery")
    class MixedDiscovery {

        @Test
        @DisplayName("should discover both annotated and interface handlers in same class")
        void shouldDiscoverBothAnnotatedAndInterfaceHandlersInSameClass() {
            var handler = new MixedHandler();
            var registry = createRegistry(MERGE_ALL);

            registry.registerHandlersIn(handler);

            // Interface-based command handler
            assertThat(registry.getCommandHandler(TestCommand.class)).isNotNull();
            // Annotated query handler
            assertThat(registry.getQueryHandler(TestQuery.class)).isNotNull();
        }

        @Test
        @DisplayName("should prefer interface-based over annotation-based for same message type")
        void shouldPreferInterfaceBasedOverAnnotationBasedForSameMessageType() {
            var handler = new ConflictingHandler();
            var registry = createRegistry(MERGE_ALL);

            // Should throw due to conflict between interface and annotation
            assertThatThrownBy(() -> registry.registerHandlersIn(handler))
                    .isInstanceOf(MultipleHandlersFoundException.class);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle null handler gracefully")
        void shouldHandleNullHandlerGracefully() {
            assertThatThrownBy(() -> registry.registerHandlersIn(null))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("Handler cannot be null");
        }

        @Test
        @DisplayName("should handle object without handlers")
        void shouldHandleObjectWithoutHandlers() {
            var handler = new NoHandlerClass();

            registry.registerHandlersIn(handler);

            // Should not throw, just register nothing
            assertThat(registry.getCommandHandler(TestCommand.class)).isNull();
        }

        @Test
        @DisplayName("should prevent duplicate command handler registration")
        void shouldPreventDuplicateCommandHandlerRegistration() {
            var handler1 = new CommandHandlerImpl();
            var handler2 = new AnotherCommandHandlerImpl();

            registry.registerHandlersIn(handler1);

            assertThatThrownBy(() -> registry.registerHandlersIn(handler2))
                    .isInstanceOf(MultipleHandlersFoundException.class);
        }
    }

    // Test fixtures
    @Command
    static class TestCommand {
        private final String value;

        TestCommand(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    @Query
    static class TestQuery {
        private final String value;

        TestQuery(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    @Event
    static class TestEvent {
        private final String value;

        TestEvent(String value) {
            this.value = value;
        }
    }

    // Annotated handlers
    static class AnnotatedCommandHandler {
        @CommandHandler
        public String handle(TestCommand command) {
            return "Command: " + command.getValue();
        }
    }

    static class AnnotatedQueryHandler {
        @QueryHandler
        public String handle(TestQuery query) {
            return "Query: " + query.getValue();
        }
    }

    static class AnnotatedEventHandler {
        @EventHandler
        public void handle(TestEvent event) {
            // Event handler
        }
    }

    static class MultipleAnnotatedHandlers {
        @CommandHandler
        public String handleCommand(TestCommand command) {
            return "Command: " + command.getValue();
        }

        @QueryHandler
        public String handleQuery(TestQuery query) {
            return "Query: " + query.getValue();
        }

        @EventHandler
        public void handleEvent(TestEvent event) {
            // Event handler
        }
    }

    static class InvalidAnnotatedHandler {
        @EventHandler
        public String handle(TestEvent event) {
            return "Invalid"; // Events must return void
        }
    }

    // Interface-based handlers
    static class CommandHandlerImpl
            implements io.github.dispatch4j.handler.CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return "Interface Command: " + command.getValue();
        }
    }

    static class QueryHandlerImpl
            implements io.github.dispatch4j.handler.QueryHandler<TestQuery, String> {
        @Override
        public String handle(TestQuery query) {
            return "Interface Query: " + query.getValue();
        }
    }

    static class EventHandlerImpl implements io.github.dispatch4j.handler.EventHandler<TestEvent> {
        @Override
        public void handle(TestEvent event) {
            // Event handler
        }
    }

    // Can't implement both CommandHandler and EventHandler with same method name
    // Test them separately

    static class AnotherCommandHandlerImpl
            implements io.github.dispatch4j.handler.CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return "Another: " + command.getValue();
        }
    }

    // Mixed handler
    static class MixedHandler
            implements io.github.dispatch4j.handler.CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return "Interface: " + command.getValue();
        }

        @QueryHandler
        public String handleQuery(TestQuery query) {
            return "Annotated: " + query.getValue();
        }
    }

    // Conflicting handler
    static class ConflictingHandler
            implements io.github.dispatch4j.handler.CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return "Interface: " + command.getValue();
        }

        @CommandHandler
        public String handleCommand(TestCommand command) {
            return "Annotated: " + command.getValue();
        }
    }

    static class NoHandlerClass {
        public void normalMethod() {
            // Not a handler
        }
    }
}
