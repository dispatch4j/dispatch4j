package io.github.dispatch4j.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.exception.Dispatch4jException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MiddlewareChainMutatorTest {

    @Mock private HandlerMiddleware middleware1;
    @Mock private HandlerMiddleware middleware2;
    @Mock private HandlerMiddleware middleware3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldStartWithEmptyMiddlewareList() {
        // Given
        var emptyChain = MiddlewareChain.empty();

        // When
        var mutated = emptyChain.mutate().build();

        // Then
        assertThat(mutated.isEmpty()).isTrue();
        assertThat(mutated.size()).isZero();
    }

    @Test
    void shouldCopyExistingMiddlewares() {
        // Given
        var originalChain = MiddlewareChain.with(middleware1, middleware2);

        // When
        var mutated = originalChain.mutate().build();

        // Then
        assertThat(mutated.size()).isEqualTo(2);
        assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
    }

    @Nested
    class WhenAddingMiddlewares {

        @Test
        void shouldAddSingleMiddleware() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1);

            // When
            var mutated = originalChain.mutate().add(middleware2).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
        }

        @Test
        void shouldAddMultipleMiddlewaresVarargs() {
            // Given
            var originalChain = MiddlewareChain.empty();

            // When
            var mutated =
                    originalChain.mutate().addAll(middleware1, middleware2, middleware3).build();

            // Then
            assertThat(mutated.size()).isEqualTo(3);
            assertThat(mutated.getMiddlewares())
                    .containsExactly(middleware1, middleware2, middleware3);
        }

        @Test
        void shouldAddMultipleMiddlewaresList() {
            // Given
            var originalChain = MiddlewareChain.empty();
            var middlewaresToAdd = List.of(middleware1, middleware2);

            // When
            var mutated = originalChain.mutate().addAll(middlewaresToAdd).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
        }

        @Test
        void shouldChainMultipleAddOperations() {
            // Given
            var originalChain = MiddlewareChain.empty();

            // When
            var mutated =
                    originalChain
                            .mutate()
                            .add(middleware1)
                            .add(middleware2)
                            .addAll(middleware3)
                            .build();

            // Then
            assertThat(mutated.size()).isEqualTo(3);
            assertThat(mutated.getMiddlewares())
                    .containsExactly(middleware1, middleware2, middleware3);
        }
    }

    @Nested
    class WhenRemovingMiddlewares {

        @Test
        void shouldRemoveSpecificMiddleware() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2, middleware3);

            // When
            var mutated = originalChain.mutate().remove(middleware2).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware3);
        }

        @Test
        void shouldRemoveMiddlewareByClass() {
            // Given
            var loggingMiddleware1 = new TestLoggingMiddleware();
            var loggingMiddleware2 = new TestLoggingMiddleware();
            var originalChain =
                    MiddlewareChain.with(
                            middleware1, loggingMiddleware1, loggingMiddleware2, middleware2);

            // When
            var mutated = originalChain.mutate().remove(TestLoggingMiddleware.class).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
        }

        @Test
        void shouldRemoveMiddlewareAtIndex() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2, middleware3);

            // When
            var mutated = originalChain.mutate().removeAt(1).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware3);
        }

        @Test
        void shouldThrowExceptionWhenRemovingAtInvalidIndex() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When & Then
            assertThatThrownBy(() -> originalChain.mutate().removeAt(-1))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessage("Index out of bounds: -1");

            assertThatThrownBy(() -> originalChain.mutate().removeAt(2))
                    .isInstanceOf(Dispatch4jException.class)
                    .hasMessage("Index out of bounds: 2");
        }

        @Test
        void shouldHandleRemovalOfNonExistentMiddleware() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When
            var mutated = originalChain.mutate().remove(middleware3).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
        }

        @Test
        void shouldHandleRemovalOfNonExistentMiddlewareClass() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When
            var mutated = originalChain.mutate().remove(TestLoggingMiddleware.class).build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware1, middleware2);
        }
    }

    @Nested
    class WhenClearingMiddlewares {

        @Test
        void shouldClearAllMiddlewares() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2, middleware3);

            // When
            var mutated = originalChain.mutate().clear().build();

            // Then
            assertThat(mutated.isEmpty()).isTrue();
            assertThat(mutated.size()).isEqualTo(0);
        }

        @Test
        void shouldAllowAddingAfterClear() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When
            var mutated = originalChain.mutate().clear().add(middleware3).build();

            // Then
            assertThat(mutated.size()).isEqualTo(1);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware3);
        }
    }

    @Nested
    class WhenChainingOperations {

        @Test
        void shouldAllowComplexChainedOperations() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When
            var mutated =
                    originalChain
                            .mutate()
                            .add(middleware3) // [m1, m2, m3]
                            .remove(middleware2) // [m1, m3]
                            .addAll(middleware2) // [m1, m3, m2]
                            .removeAt(0) // [m3, m2]
                            .build();

            // Then
            assertThat(mutated.size()).isEqualTo(2);
            assertThat(mutated.getMiddlewares()).containsExactly(middleware3, middleware2);
        }
    }

    @Nested
    class WhenBuildingChain {

        @Test
        void shouldCreateNewChainInstance() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1, middleware2);

            // When
            var mutated = originalChain.mutate().add(middleware3).build();

            // Then
            assertThat(mutated).isNotSameAs(originalChain);
            assertThat(originalChain.size()).isEqualTo(2);
            assertThat(mutated.size()).isEqualTo(3);
        }

        @Test
        void shouldCreateNoopChainWhenEmpty() {
            // Given
            var originalChain = MiddlewareChain.with(middleware1);

            // When
            var mutated = originalChain.mutate().clear().build();

            // Then
            assertThat(mutated).isInstanceOf(NoopMiddlewareChain.class);
            assertThat(mutated.isEmpty()).isTrue();
        }
    }

    // Test helper class for testing class-based removal
    private static class TestLoggingMiddleware implements HandlerMiddleware {
        @Override
        public <T, R> R handle(T message, MiddlewareContext context, Next<T, R> next) {
            return next.handle(message);
        }
    }
}
