package io.github.dispatch4j.core.handler;

import static org.assertj.core.api.Assertions.*;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.exception.Dispatch4jException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HandlerInvokerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(HandlerInvokerIntegrationTest.class);

    @Nested
    class CommandHandlerValidation {

        @Test
        void shouldCreateValidCommandHandler() throws Exception {
            // Given
            var handlerInstance = new ValidCommandHandlers();
            Method method =
                    ValidCommandHandlers.class.getDeclaredMethod(
                            "handleTestCommand", TestCommand.class);

            // When
            var handler = HandlerInvoker.createCommandHandler(method, handlerInstance);
            String result = (String) handler.handle(new TestCommand("test"));

            // Then
            assertThat(result).isEqualTo("Handled: test");
        }

        @Test
        void shouldValidateCommandHandlerMethodParameterCount() throws Exception {
            // Given
            var handlerInstance = new InvalidCommandHandlers();
            Method method =
                    InvalidCommandHandlers.class.getDeclaredMethod("handleWithNoParameters");

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createCommandHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must have exactly one parameter");
        }

        @Test
        void shouldValidateCommandHandlerParameterAnnotation() throws Exception {
            // Given
            var handlerInstance = new InvalidCommandHandlers();
            Method method =
                    InvalidCommandHandlers.class.getDeclaredMethod(
                            "handleUnannotatedParameter", String.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createCommandHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must be annotated with @Command");
        }

        @Test
        void shouldValidateCommandHandlerReturnType() throws Exception {
            // Given
            var handlerInstance = new InvalidCommandHandlers();
            Method method =
                    InvalidCommandHandlers.class.getDeclaredMethod(
                            "handleWithVoidReturn", TestCommand.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createCommandHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must return a value");
        }
    }

    @Nested
    class QueryHandlerValidation {

        @Test
        void shouldCreateValidQueryHandler() throws Exception {
            // Given
            var handlerInstance = new ValidQueryHandlers();
            Method method =
                    ValidQueryHandlers.class.getDeclaredMethod("handleTestQuery", TestQuery.class);

            // When
            var handler = HandlerInvoker.createQueryHandler(method, handlerInstance);
            Integer result = (Integer) handler.handle(new TestQuery(5));

            // Then
            assertThat(result).isEqualTo(10);
        }

        @Test
        void shouldValidateQueryHandlerParameterAnnotation() throws Exception {
            // Given
            var handlerInstance = new InvalidQueryHandlers();
            Method method =
                    InvalidQueryHandlers.class.getDeclaredMethod(
                            "handleUnannotatedParameter", String.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createQueryHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must be annotated with @Query");
        }

        @Test
        void shouldValidateQueryHandlerReturnType() throws Exception {
            // Given
            var handlerInstance = new InvalidQueryHandlers();
            Method method =
                    InvalidQueryHandlers.class.getDeclaredMethod(
                            "handleWithVoidReturn", TestQuery.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createQueryHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must return a value");
        }
    }

    @Nested
    class EventHandlerValidation {

        @Test
        void shouldCreateValidEventHandler() throws Exception {
            // Given
            var handlerInstance = new ValidEventHandlers();
            Method method =
                    ValidEventHandlers.class.getDeclaredMethod("handleTestEvent", TestEvent.class);

            // When
            var handler = HandlerInvoker.createEventHandler(method, handlerInstance);

            // Then - Should not throw
            assertThatCode(() -> handler.handle(new TestEvent("test-message")))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldValidateEventHandlerParameterAnnotation() throws Exception {
            // Given
            var handlerInstance = new InvalidEventHandlers();
            Method method =
                    InvalidEventHandlers.class.getDeclaredMethod(
                            "handleUnannotatedParameter", String.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createEventHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must be annotated with @Event");
        }

        @Test
        void shouldValidateEventHandlerReturnType() throws Exception {
            // Given
            var handlerInstance = new InvalidEventHandlers();
            Method method =
                    InvalidEventHandlers.class.getDeclaredMethod(
                            "handleWithNonVoidReturn", TestEvent.class);

            // When & Then
            assertThatThrownBy(() -> HandlerInvoker.createEventHandler(method, handlerInstance))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("must return void");
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void shouldUnwrapInvocationTargetException() throws Exception {
            // Given
            var handlerInstance = new ExceptionThrowingHandlers();
            Method method =
                    ExceptionThrowingHandlers.class.getDeclaredMethod(
                            "throwRuntimeException", TestCommand.class);
            var handler = HandlerInvoker.createCommandHandler(method, handlerInstance);

            // When & Then
            assertThatThrownBy(() -> handler.handle(new TestCommand("trigger-error")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test runtime exception");
        }

        @Test
        void shouldWrapCheckedExceptions() throws Exception {
            // Given
            var handlerInstance = new ExceptionThrowingHandlers();
            Method method =
                    ExceptionThrowingHandlers.class.getDeclaredMethod(
                            "throwCheckedException", TestCommand.class);
            var handler = HandlerInvoker.createCommandHandler(method, handlerInstance);

            // When & Then
            assertThatThrownBy(() -> handler.handle(new TestCommand("trigger-checked")))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessageContaining("Failed to invoke handler method");
        }
    }

    @Nested
    class AccessibilityHandling {

        @Test
        void shouldHandlePrivateHandlerMethods() throws Exception {
            // Given
            var handlerInstance = new PrivateMethodHandlers();
            Method method =
                    PrivateMethodHandlers.class.getDeclaredMethod(
                            "handlePrivateCommand", TestCommand.class);

            // When
            var handler = HandlerInvoker.createCommandHandler(method, handlerInstance);
            String result = (String) handler.handle(new TestCommand("private-test"));

            // Then
            assertThat(result).isEqualTo("Private handler: private-test");
        }
    }

    // Test message types
    @Command
    public record TestCommand(String value) {}

    @Query
    public record TestQuery(int number) {}

    @Event
    public record TestEvent(String message) {}

    // Valid handler classes
    public static class ValidCommandHandlers {
        public String handleTestCommand(TestCommand command) {
            return "Handled: " + command.value();
        }
    }

    public static class ValidQueryHandlers {
        public Integer handleTestQuery(TestQuery query) {
            return query.number() * 2;
        }
    }

    public static class ValidEventHandlers {
        public void handleTestEvent(TestEvent event) {
            log.info("Event processed: {}", event.message());
        }
    }

    // Invalid handler classes
    public static class InvalidCommandHandlers {
        public String handleWithNoParameters() {
            return "invalid";
        }

        public String handleUnannotatedParameter(String unannotated) {
            return "invalid";
        }

        public void handleWithVoidReturn(TestCommand command) {
            // Invalid - command handlers must return value
        }
    }

    public static class InvalidQueryHandlers {
        public String handleUnannotatedParameter(String unannotated) {
            return "invalid";
        }

        public void handleWithVoidReturn(TestQuery query) {
            // Invalid - query handlers must return value
        }
    }

    public static class InvalidEventHandlers {
        public void handleUnannotatedParameter(String unannotated) {
            // Invalid - parameter not annotated
        }

        public String handleWithNonVoidReturn(TestEvent event) {
            return "invalid"; // Invalid - event handlers must return void
        }
    }

    // Exception testing handlers
    public static class ExceptionThrowingHandlers {
        public String throwRuntimeException(TestCommand command) {
            throw new RuntimeException("Test runtime exception");
        }

        public String throwCheckedException(TestCommand command) throws Exception {
            throw new Exception("Test checked exception");
        }
    }

    // Private method handlers
    public static class PrivateMethodHandlers {
        private String handlePrivateCommand(TestCommand command) {
            return "Private handler: " + command.value();
        }
    }
}
