package io.github.dispatch4j.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dispatch4j.annotation.Command;
import io.github.dispatch4j.annotation.Event;
import io.github.dispatch4j.annotation.HandlerType;
import io.github.dispatch4j.annotation.Query;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.EventHandler;
import io.github.dispatch4j.handler.QueryHandler;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InterfaceBasedDiscoveryStrategy")
class InterfaceBasedDiscoveryStrategyTest {

    private InterfaceBasedDiscoveryStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new InterfaceBasedDiscoveryStrategy();
    }

    @Nested
    @DisplayName("Discovery")
    class Discovery {

        @Test
        @DisplayName("should discover CommandHandler interface implementation")
        void shouldDiscoverCommandHandlerInterface() {
            var handler = new TestCommandHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.COMMAND);
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
            assertThat(registration.handlerInstance()).isInstanceOf(CommandHandler.class);
        }

        @Test
        @DisplayName("should discover QueryHandler interface implementation")
        void shouldDiscoverQueryHandlerInterface() {
            var handler = new TestQueryHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.QUERY);
            assertThat(registration.messageType()).isEqualTo(TestQuery.class);
            assertThat(registration.handlerInstance()).isInstanceOf(QueryHandler.class);
        }

        @Test
        @DisplayName("should discover EventHandler interface implementation")
        void shouldDiscoverEventHandlerInterface() {
            var handler = new TestEventHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.handlerKind()).isEqualTo(HandlerType.EVENT);
            assertThat(registration.messageType()).isEqualTo(TestEvent.class);
            assertThat(registration.handlerInstance()).isInstanceOf(EventHandler.class);
        }

        @Test
        @DisplayName("should discover single interface implementation")
        void shouldDiscoverSingleInterfaceImplementation() {
            var handler = new TestCommandHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            assertThat(registrations)
                    .extracting(HandlerRegistration::handlerKind)
                    .contains(HandlerType.COMMAND);
        }

        @Test
        @DisplayName("should return empty collection for non-handler object")
        void shouldReturnEmptyCollectionForNonHandler() {
            var handler = new NonHandlerClass();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).isEmpty();
        }

        @Test
        @DisplayName("should handle nested interface implementation")
        void shouldHandleNestedInterfaceImplementation() {
            var handler = new TestNestedCommandHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("should handle abstract base class implementation")
        void shouldHandleAbstractBaseClassImplementation() {
            var handler = new ConcreteCommandHandler();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
        }
    }

    @Nested
    @DisplayName("Type Resolution")
    class TypeResolution {

        @Test
        @DisplayName("should resolve generic type from direct implementation")
        void shouldResolveGenericTypeFromDirectImplementation() {
            var handler = new TestCommandHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("should resolve generic type through inheritance hierarchy")
        void shouldResolveGenericTypeThroughInheritanceHierarchy() {
            var handler = new TestNestedCommandHandlerImpl();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.messageType()).isEqualTo(TestCommand.class);
        }

        @Test
        @DisplayName("should handle raw type implementation gracefully")
        void shouldHandleRawTypeImplementationGracefully() {
            @SuppressWarnings("rawtypes")
            var handler = new RawCommandHandler();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            // Should skip raw type implementations as we can't determine the message type
            assertThat(registrations).isEmpty();
        }

        @Test
        @DisplayName("should resolve complex generic types")
        void shouldResolveComplexGenericTypes() {
            var handler = new ComplexGenericHandler();

            Collection<HandlerRegistration> registrations = strategy.discoverHandlers(handler);

            assertThat(registrations).hasSize(1);
            var registration = registrations.iterator().next();
            assertThat(registration.messageType()).isEqualTo(ComplexCommand.class);
        }
    }

    @Nested
    @DisplayName("Support Detection")
    class SupportDetection {

        @Test
        @DisplayName("should support handler interface implementations")
        void shouldSupportHandlerInterfaceImplementations() {
            assertThat(strategy.supports(new TestCommandHandlerImpl())).isTrue();
            assertThat(strategy.supports(new TestQueryHandlerImpl())).isTrue();
            assertThat(strategy.supports(new TestEventHandlerImpl())).isTrue();
        }

        @Test
        @DisplayName("should support command handler interface implementation")
        void shouldSupportCommandHandlerInterfaceImplementation() {
            assertThat(strategy.supports(new TestCommandHandlerImpl())).isTrue();
        }

        @Test
        @DisplayName("should not support non-handler objects")
        void shouldNotSupportNonHandlerObjects() {
            assertThat(strategy.supports(new NonHandlerClass())).isFalse();
            assertThat(strategy.supports("string")).isFalse();
            assertThat(strategy.supports(42)).isFalse();
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

    @Command
    static class ComplexCommand<T> {}

    static class TestCommandHandlerImpl implements CommandHandler<TestCommand, TestResult> {
        @Override
        public TestResult handle(TestCommand command) {
            return new TestResult();
        }
    }

    static class TestQueryHandlerImpl implements QueryHandler<TestQuery, TestResult> {
        @Override
        public TestResult handle(TestQuery query) {
            return new TestResult();
        }
    }

    static class TestEventHandlerImpl implements EventHandler<TestEvent> {
        @Override
        public void handle(TestEvent event) {
            // Event handler
        }
    }

    // Can't implement both interfaces with same method name in Java
    // So we test them separately in different test cases

    abstract static class AbstractCommandHandler
            implements CommandHandler<TestCommand, TestResult> {
        // Abstract base class
    }

    static class ConcreteCommandHandler extends AbstractCommandHandler {
        @Override
        public TestResult handle(TestCommand command) {
            return new TestResult();
        }
    }

    static class TestNestedCommandHandlerImpl extends TestCommandHandlerImpl {
        // Nested implementation
    }

    @SuppressWarnings("rawtypes")
    static class RawCommandHandler implements CommandHandler {
        @Override
        public Object handle(Object command) {
            return new Object();
        }
    }

    static class ComplexGenericHandler
            implements CommandHandler<ComplexCommand<String>, TestResult> {
        @Override
        public TestResult handle(ComplexCommand<String> command) {
            return new TestResult();
        }
    }

    static class NonHandlerClass {
        public void someMethod() {
            // Not a handler
        }
    }
}
