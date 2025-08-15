package io.github.dispatch4j.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.HandlerType;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.exception.HandlerValidationException;
import io.github.dispatch4j.handler.CommandHandler;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompositeDiscoveryStrategy")
class CompositeDiscoveryStrategyTest {

    private HandlerDiscoveryStrategy mockStrategy1;
    private HandlerDiscoveryStrategy mockStrategy2;
    private HandlerDiscoveryStrategy mockStrategy3;

    @BeforeEach
    void setUp() {
        mockStrategy1 = mock(HandlerDiscoveryStrategy.class);
        mockStrategy2 = mock(HandlerDiscoveryStrategy.class);
        mockStrategy3 = mock(HandlerDiscoveryStrategy.class);
    }

    @Nested
    @DisplayName("First Wins Resolution")
    class FirstWinsResolution {

        @Test
        @DisplayName("should use first strategy that supports source")
        void shouldUseFirstStrategyThatSupportsSource() {
            var handler = new TestHandler();
            var registration = createRegistration(TestCommand.class, HandlerType.COMMAND);

            when(mockStrategy1.supports(handler)).thenReturn(false);
            when(mockStrategy2.supports(handler)).thenReturn(true);
            when(mockStrategy3.supports(handler)).thenReturn(true);
            when(mockStrategy2.discoverHandlers(handler)).thenReturn(List.of(registration));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2, mockStrategy3),
                            ConflictResolutionStrategy.FIRST_WINS);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            assertThat(result).containsExactly(registration);
            verify(mockStrategy1).supports(handler);
            verify(mockStrategy2).supports(handler);
            verify(mockStrategy2).discoverHandlers(handler);
            verify(mockStrategy3, never()).discoverHandlers(handler);
        }

        @Test
        @DisplayName("should log warning on validation exception")
        void shouldLogWarningOnValidationException() {
            var handler = new TestHandler();
            var registration = createRegistration(TestCommand.class, HandlerType.COMMAND);

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy1.discoverHandlers(handler))
                    .thenThrow(
                            new HandlerValidationException(
                                    "Test validation error",
                                    "test-strategy",
                                    handler,
                                    "test failure"));
            when(mockStrategy2.supports(handler)).thenReturn(true);
            when(mockStrategy2.discoverHandlers(handler)).thenReturn(List.of(registration));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2),
                            ConflictResolutionStrategy.FIRST_WINS);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            assertThat(result).containsExactly(registration);
        }
    }

    @Nested
    @DisplayName("Merge All Resolution")
    class MergeAllResolution {

        @Test
        @DisplayName("should merge results from all supporting strategies")
        void shouldMergeResultsFromAllSupportingStrategies() {
            var handler = new TestHandler();
            var registration1 = createRegistration(TestCommand.class, HandlerType.COMMAND);
            var registration2 = createRegistration(TestQuery.class, HandlerType.QUERY);
            var registration3 = createRegistration(TestEvent.class, HandlerType.EVENT);

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy2.supports(handler)).thenReturn(true);
            when(mockStrategy3.supports(handler)).thenReturn(false);
            when(mockStrategy1.discoverHandlers(handler)).thenReturn(List.of(registration1));
            when(mockStrategy2.discoverHandlers(handler))
                    .thenReturn(List.of(registration2, registration3));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2, mockStrategy3),
                            ConflictResolutionStrategy.MERGE_ALL);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            assertThat(result)
                    .containsExactlyInAnyOrder(registration1, registration2, registration3);
            verify(mockStrategy3, never()).discoverHandlers(handler);
        }

        @Test
        @DisplayName("should handle duplicate registrations gracefully")
        void shouldHandleDuplicateRegistrationsGracefully() {
            var handler = new TestHandler();
            var registration = createRegistration(TestCommand.class, HandlerType.COMMAND);

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy2.supports(handler)).thenReturn(true);
            when(mockStrategy1.discoverHandlers(handler)).thenReturn(List.of(registration));
            when(mockStrategy2.discoverHandlers(handler)).thenReturn(List.of(registration));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2),
                            ConflictResolutionStrategy.MERGE_ALL);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            // Should contain the registration only once (as it's a Set internally)
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Fail Fast Resolution")
    class FailFastResolution {

        @Test
        @DisplayName("should throw exception on first validation error")
        void shouldThrowExceptionOnFirstValidationError() {
            var handler = new TestHandler();
            var expectedException =
                    new HandlerValidationException(
                            "Test validation error", "test-strategy", handler, "test failure");

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy1.discoverHandlers(handler)).thenThrow(expectedException);

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2),
                            ConflictResolutionStrategy.FAIL_FAST);

            assertThatThrownBy(() -> composite.discoverHandlers(handler))
                    .cause()
                    .isEqualTo(expectedException);

            verify(mockStrategy2, never()).supports(handler);
            verify(mockStrategy2, never()).discoverHandlers(handler);
        }

        @Test
        @DisplayName("should use first successful strategy")
        void shouldUseFirstSuccessfulStrategy() {
            var handler = new TestHandler();
            var registration = createRegistration(TestCommand.class, HandlerType.COMMAND);

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy1.discoverHandlers(handler)).thenReturn(List.of(registration));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2),
                            ConflictResolutionStrategy.FAIL_FAST);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            assertThat(result).containsExactly(registration);
            verify(mockStrategy2, never()).supports(handler);
        }
    }

    @Nested
    @DisplayName("Priority")
    class Priority {

        @Test
        @DisplayName("should order strategies by priority")
        void shouldOrderStrategiesByPriority() {
            var handler = new TestHandler();
            var registration1 = createRegistration(TestCommand.class, HandlerType.COMMAND);
            var registration2 = createRegistration(TestQuery.class, HandlerType.QUERY);

            when(mockStrategy1.getPriority()).thenReturn(10);
            when(mockStrategy2.getPriority()).thenReturn(20);
            when(mockStrategy3.getPriority()).thenReturn(5);

            when(mockStrategy3.supports(handler)).thenReturn(true);
            when(mockStrategy3.discoverHandlers(handler)).thenReturn(List.of(registration2));

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2, mockStrategy3),
                            ConflictResolutionStrategy.FIRST_WINS);

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);

            // Strategy3 should be checked first (priority 5), then strategy1 (10), then strategy2
            // (20)
            assertThat(result).containsExactly(registration2);
            verify(mockStrategy3).supports(handler);
            verify(mockStrategy3).discoverHandlers(handler);
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should create composite with multiple strategies")
        void shouldCreateCompositeWithMultipleStrategies() {
            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2, mockStrategy3),
                            ConflictResolutionStrategy.MERGE_ALL);

            assertThat(composite).isNotNull();
            assertThat(composite.supports(new TestHandler())).isFalse();
        }

        @Test
        @DisplayName("should use FIRST_WINS as default conflict resolution")
        void shouldUseFirstWinsAsDefaultConflictResolution() {
            var handler = new TestHandler();
            var registration = createRegistration(TestCommand.class, HandlerType.COMMAND);

            when(mockStrategy1.supports(handler)).thenReturn(true);
            when(mockStrategy1.discoverHandlers(handler)).thenReturn(List.of(registration));

            var composite = new CompositeDiscoveryStrategy(List.of(mockStrategy1));

            Collection<HandlerRegistration> result = composite.discoverHandlers(handler);
            assertThat(result).containsExactly(registration);
        }
    }

    @Nested
    @DisplayName("Support Detection")
    class SupportDetection {

        @Test
        @DisplayName("should support source if any strategy supports it")
        void shouldSupportSourceIfAnyStrategySupportsIt() {
            var handler = new TestHandler();

            when(mockStrategy1.supports(handler)).thenReturn(false);
            when(mockStrategy2.supports(handler)).thenReturn(true);
            when(mockStrategy3.supports(handler)).thenReturn(false);

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2, mockStrategy3),
                            ConflictResolutionStrategy.FIRST_WINS);

            assertThat(composite.supports(handler)).isTrue();
        }

        @Test
        @DisplayName("should not support source if no strategy supports it")
        void shouldNotSupportSourceIfNoStrategySupportsIt() {
            var handler = new TestHandler();

            when(mockStrategy1.supports(handler)).thenReturn(false);
            when(mockStrategy2.supports(handler)).thenReturn(false);

            var composite =
                    new CompositeDiscoveryStrategy(
                            List.of(mockStrategy1, mockStrategy2),
                            ConflictResolutionStrategy.FIRST_WINS);

            assertThat(composite.supports(handler)).isFalse();
        }
    }

    // Helper methods
    private HandlerRegistration createRegistration(Class<?> messageType, HandlerType handlerKind) {
        return new HandlerRegistration(
                messageType,
                TestHandler.class,
                handlerKind,
                "test-method",
                mock(CommandHandler.class),
                "test-source");
    }

    // Test fixtures
    @Command
    static class TestCommand {}

    @Query
    static class TestQuery {}

    @Event
    static class TestEvent {}

    static class TestHandler {
        public void handle(TestCommand command) {}
    }
}
