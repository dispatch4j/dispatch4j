package io.github.dispatch4j.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MiddlewareChainTest {

    private static final String MESSAGE = "test";
    @Mock private HandlerMiddleware middleware1;
    @Mock private HandlerMiddleware middleware2;
    @Mock private HandlerMiddleware middleware3;
    @Mock private Function<String, String> finalHandler;
    @Mock private MiddlewareContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldExecuteFinalHandlerWhenNoMiddleware() {
        // Given
        var middlewareChain = MiddlewareChain.empty();
        when(finalHandler.apply(MESSAGE)).thenReturn("result");

        // When
        var result = middlewareChain.execute(MESSAGE, context, finalHandler);

        // Then
        assertThat(result).isEqualTo("result");
        verify(finalHandler).apply(MESSAGE);
        verifyNoInteractions(middleware1, middleware2, middleware3);
    }

    @Test
    void shouldExecuteMiddlewareInOrder() {
        // Given
        var middlewareChain = MiddlewareChain.with(middleware1, middleware2);

        // Mock middleware chain behavior
        when(middleware1.handle(eq(MESSAGE), eq(context), any()))
                .thenAnswer(
                        invocation -> {
                            HandlerMiddleware.Next<String, String> next = invocation.getArgument(2);
                            return "m1-" + next.handle(MESSAGE);
                        });

        when(middleware2.handle(eq(MESSAGE), eq(context), any()))
                .thenAnswer(
                        invocation -> {
                            HandlerMiddleware.Next<String, String> next = invocation.getArgument(2);
                            return next.handle(MESSAGE) + "-m2";
                        });

        when(finalHandler.apply(MESSAGE)).thenReturn("result");

        // When
        var result = middlewareChain.execute(MESSAGE, context, finalHandler);

        // Then
        assertThat(result).isEqualTo("m1-result-m2");

        var inOrder = inOrder(middleware1, middleware2, finalHandler);
        inOrder.verify(middleware1).handle(eq(MESSAGE), eq(context), any());
        inOrder.verify(middleware2).handle(eq(MESSAGE), eq(context), any());
        inOrder.verify(finalHandler).apply(MESSAGE);
    }

    @Test
    void shouldAllowMiddlewareToModifyExecution() {
        // Given
        var middlewareChain = MiddlewareChain.with(middleware1);

        // Middleware that short-circuits the chain
        when(middleware1.handle(eq(MESSAGE), eq(context), any())).thenReturn("short-circuited");

        // When
        var result = middlewareChain.execute(MESSAGE, context, finalHandler);

        // Then
        assertThat(result).isEqualTo("short-circuited");
        verify(middleware1).handle(eq(MESSAGE), eq(context), any());
        verifyNoInteractions(finalHandler);
    }

    @Test
    void shouldPropagateExceptionsFromMiddleware() {
        // Given
        var expectedException = new RuntimeException("Middleware error");
        var middlewareChain = MiddlewareChain.with(middleware1);

        when(middleware1.handle(eq(MESSAGE), eq(context), any())).thenThrow(expectedException);

        // When & Then
        assertThatThrownBy(() -> middlewareChain.execute(MESSAGE, context, finalHandler))
                .isSameAs(expectedException);

        verifyNoInteractions(finalHandler);
    }

    @Test
    void shouldPropagateExceptionsFromFinalHandler() {
        // Given
        RuntimeException expectedException = new RuntimeException("Handler error");
        var middlewareChain = MiddlewareChain.with(PassthroughMiddleware.INSTANCE);

        when(finalHandler.apply(MESSAGE)).thenThrow(expectedException);

        // When & Then
        assertThatThrownBy(() -> middlewareChain.execute(MESSAGE, context, finalHandler))
                .isSameAs(expectedException);
    }

    @Test
    void shouldBuildChainUsingBuilder() {
        // When
        var chain =
                MiddlewareChain.builder()
                        .addAll(List.of(middleware1, middleware2, middleware3))
                        .build();

        // Then
        assertThat(chain.size()).isEqualTo(3);
    }

    @Test
    void shouldBuildChainUsingBuilderWithVarargs() {
        // When
        var chain = MiddlewareChain.builder().addAll(middleware1, middleware2, middleware3).build();

        // Then
        assertThat(chain.size()).isEqualTo(3);
    }

    static class PassthroughMiddleware implements HandlerMiddleware {

        static final HandlerMiddleware INSTANCE = new PassthroughMiddleware();

        @Override
        public <T, R> R handle(T message, MiddlewareContext context, Next<T, R> next) {
            return next.handle(message);
        }
    }
}
