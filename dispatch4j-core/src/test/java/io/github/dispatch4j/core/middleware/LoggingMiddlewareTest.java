package io.github.dispatch4j.core.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggingMiddlewareTest {

    private final LoggingMiddleware middleware = new LoggingMiddleware();

    @Test
    void shouldCallNextHandlerAndReturnResult() {
        // Given
        var message = "test-message";
        var expectedResult = "test-result";
        var context = new MiddlewareContext(MiddlewareContext.MessageType.COMMAND, String.class);
        
        HandlerMiddleware.Next<String, String> next = msg -> {
            assertThat(msg).isEqualTo(message);
            return expectedResult;
        };

        // When
        var actualResult = middleware.handle(message, context, next);

        // Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void shouldPropagateExceptions() {
        // Given
        var message = "test-message";
        var expectedException = new RuntimeException("Handler error");
        var context = new MiddlewareContext(MiddlewareContext.MessageType.QUERY, String.class);
        
        HandlerMiddleware.Next<String, String> next = msg -> {
            throw expectedException;
        };

        // When & Then
        assertThatThrownBy(() -> middleware.handle(message, context, next))
                .isEqualTo(expectedException);
    }

    @Test
    void shouldHandleNullReturnValue() {
        // Given
        var message = "test-message";
        var context = new MiddlewareContext(MiddlewareContext.MessageType.EVENT, String.class);
        
        HandlerMiddleware.Next<String, String> next = msg -> null;

        // When
        var result = middleware.handle(message, context, next);

        // Then
        assertThat(result).isNull();
    }
}