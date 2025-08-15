package io.github.dispatch4j.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.HandlerType;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.exception.HandlerDiscoveryException;
import io.github.dispatch4j.exception.HandlerValidationException;
import io.github.dispatch4j.util.AnnotationFinder;
import io.github.dispatch4j.util.CoreAnnotationFinder;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AnnotationBasedDiscoveryStrategy")
class AnnotationBasedDiscoveryStrategyTest {

    private AnnotationBasedDiscoveryStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AnnotationBasedDiscoveryStrategy();
    }

    @Nested
    @DisplayName("Discovery")
    class Discovery {

        @Test
        @DisplayName("should discover command handler methods")
        void shouldDiscoverCommandHandlerMethods() {
            var handler = new TestCommandHandlerClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.COMMAND);
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("should discover query handler methods")
        void shouldDiscoverQueryHandlerMethods() {
            var handler = new TestQueryHandlerClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.QUERY);
            assertThat(registration.messageType()).isEqualTo(TestQuery.class);
        }

        @Test
        @DisplayName("should discover event handler methods")
        void shouldDiscoverEventHandlerMethods() {
            var handler = new TestEventHandlerClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.EVENT);
            assertThat(registration.messageType()).isEqualTo(TestEvent.class);
        }

        @Test
        @DisplayName("should discover multiple handlers in same class")
        void shouldDiscoverMultipleHandlersInSameClass() {
            var handler = new TestMultipleHandlersClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(3);
            assertThat(registrations)
                    .extracting(HandlerRegistration::handlerKind)
                    .containsExactlyInAnyOrder(
                            HandlerType.COMMAND, HandlerType.QUERY, HandlerType.EVENT);
        }

        @Test
        @DisplayName("should return empty collection for class without handlers")
        void shouldReturnEmptyCollectionForClassWithoutHandlers() {
            var handler = new ClassWithoutHandlers();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).isEmpty();
        }

        @Test
        @DisplayName("should ignore private methods")
        void shouldIgnorePrivateMethods() {
            var handler = new TestPrivateHandlerClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should validate command handler has exactly one parameter")
        void shouldValidateCommandHandlerHasExactlyOneParameter() {
            var handler = new InvalidCommandHandlerNoParams();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }

        @Test
        @DisplayName("should validate command handler returns non-void")
        void shouldValidateCommandHandlerReturnsNonVoid() {
            var handler = new InvalidCommandHandlerVoidReturn();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }

        @Test
        @DisplayName("should validate query handler has exactly one parameter")
        void shouldValidateQueryHandlerHasExactlyOneParameter() {
            var handler = new InvalidQueryHandlerTwoParams();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }

        @Test
        @DisplayName("should validate query handler returns non-void")
        void shouldValidateQueryHandlerReturnsNonVoid() {
            var handler = new InvalidQueryHandlerVoidReturn();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }

        @Test
        @DisplayName("should validate event handler has exactly one parameter")
        void shouldValidateEventHandlerHasExactlyOneParameter() {
            var handler = new InvalidEventHandlerNoParams();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }

        @Test
        @DisplayName("should validate event handler returns void")
        void shouldValidateEventHandlerReturnsVoid() {
            var handler = new InvalidEventHandlerWithReturn();

            assertThatThrownBy(() -> strategy.discoverHandlers(handler))
                    .isInstanceOf(HandlerDiscoveryException.class)
                    .hasMessageContaining("Failed to process handler method")
                    .hasCauseInstanceOf(HandlerValidationException.class);
        }
    }

    @Nested
    @DisplayName("Custom AnnotationUtils")
    class CustomAnnotationFinder {

        @Test
        @DisplayName("should use provided AnnotationUtils")
        void shouldUseProvidedAnnotationUtils() {
            var customUtils = new TestAnnotationFinder();
            var customStrategy = new AnnotationBasedDiscoveryStrategy(customUtils);
            var handler = new TestCommandHandlerClass();

            customStrategy.discoverHandlers(handler);

            assertThat(customUtils.wasUsed()).isTrue();
        }

        @Test
        @DisplayName("should use CoreAnnotationUtils by default")
        void shouldUseCoreAnnotationUtilsByDefault() {
            var defaultStrategy = new AnnotationBasedDiscoveryStrategy();

            assertThat(defaultStrategy.supports(new TestCommandHandlerClass())).isTrue();
        }
    }

    // Test fixtures
    @Command
    static class TestCommand {}

    @Query
    static class TestQuery {}

    @Event
    static class TestEvent {}

    static class TestResult {}

    static class TestCommandHandlerClass {
        @io.github.dispatch4j.annotation.CommandHandler
        public TestResult handle(TestCommand command) {
            return new TestResult();
        }
    }

    static class TestQueryHandlerClass {
        @io.github.dispatch4j.annotation.QueryHandler
        public TestResult handle(TestQuery query) {
            return new TestResult();
        }
    }

    static class TestEventHandlerClass {
        @io.github.dispatch4j.annotation.EventHandler
        public void handle(TestEvent event) {
            // Event handler
        }
    }

    static class TestMultipleHandlersClass {
        @io.github.dispatch4j.annotation.CommandHandler
        public TestResult handleCommand(TestCommand command) {
            return new TestResult();
        }

        @io.github.dispatch4j.annotation.QueryHandler
        public TestResult handleQuery(TestQuery query) {
            return new TestResult();
        }

        @io.github.dispatch4j.annotation.EventHandler
        public void handleEvent(TestEvent event) {
            // Event handler
        }
    }

    static class TestPrivateHandlerClass {
        @io.github.dispatch4j.annotation.CommandHandler
        private TestResult handle(TestCommand command) {
            return new TestResult();
        }
    }

    static class ClassWithoutHandlers {
        public void normalMethod() {
            // Not a handler
        }
    }

    static class InvalidCommandHandlerNoParams {
        @io.github.dispatch4j.annotation.CommandHandler
        public TestResult handle() {
            return new TestResult();
        }
    }

    static class InvalidCommandHandlerVoidReturn {
        @io.github.dispatch4j.annotation.CommandHandler
        public void handle(TestCommand command) {
            // Invalid - commands must return a value
        }
    }

    static class InvalidQueryHandlerTwoParams {
        @io.github.dispatch4j.annotation.QueryHandler
        public TestResult handle(TestQuery query, String extra) {
            return new TestResult();
        }
    }

    static class InvalidQueryHandlerVoidReturn {
        @io.github.dispatch4j.annotation.QueryHandler
        public void handle(TestQuery query) {
            // Invalid - queries must return a value
        }
    }

    static class InvalidEventHandlerNoParams {
        @io.github.dispatch4j.annotation.EventHandler
        public void handle() {
            // Invalid - must have one parameter
        }
    }

    static class InvalidEventHandlerWithReturn {
        @io.github.dispatch4j.annotation.EventHandler
        public TestResult handle(TestEvent event) {
            return new TestResult(); // Invalid - events must return void
        }
    }

    static class TestAnnotationFinder implements AnnotationFinder {
        private boolean used = false;

        @Override
        public boolean isCommandHandler(java.lang.reflect.Method method) {
            used = true;
            return new CoreAnnotationFinder().isCommandHandler(method);
        }

        @Override
        public boolean isQueryHandler(java.lang.reflect.Method method) {
            used = true;
            return new CoreAnnotationFinder().isQueryHandler(method);
        }

        @Override
        public boolean isEventHandler(java.lang.reflect.Method method) {
            used = true;
            return new CoreAnnotationFinder().isEventHandler(method);
        }

        @Override
        public String getDetectionStrategyName() {
            return "test";
        }

        public boolean wasUsed() {
            return used;
        }
    }
}
