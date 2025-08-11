package io.github.dispatch4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.core.exception.Dispatch4jException;
import io.github.dispatch4j.core.handler.*;
import io.github.dispatch4j.core.middleware.HandlerMiddleware;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class Dispatch4jBuilderTest {

    @Mock private Executor mockExecutor;

    @Mock private HandlerRegistry mockHandlerRegistry;

    @Mock private HandlerMiddleware mockMiddleware1;

    @Mock private HandlerMiddleware mockMiddleware2;

    @Mock private CommandHandler<TestCommand, String> mockCommandHandler;

    @Mock private QueryHandler<TestQuery, String> mockQueryHandler;

    @Mock private EventHandler<TestEvent> mockEventHandler;

    private Dispatch4jBuilder builder;

    static class TestCommand {}

    static class TestQuery {}

    static class TestEvent {}

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        builder = new Dispatch4jBuilder();
    }

    @Test
    void shouldCreateBuilderWithDefaults() {
        var dispatcher = builder.build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldSetCustomExecutor() {
        var dispatcher = builder.executor(mockExecutor).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenExecutorIsNull() {
        assertThatThrownBy(() -> builder.executor(null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Executor cannot be null");
    }

    @Test
    void shouldSetCustomHandlerRegistry() {
        var dispatcher = builder.handlerRegistry(mockHandlerRegistry).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenHandlerRegistryIsNull() {
        assertThatThrownBy(() -> builder.handlerRegistry(null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Handler registry cannot be null");
    }

    @Test
    void shouldAddMiddleware() {
        var dispatcher = builder.addMiddleware(mockMiddleware1).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenMiddlewareIsNull() {
        assertThatThrownBy(() -> builder.addMiddleware(null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Middleware cannot be null");
    }

    @Test
    void shouldAddMultipleMiddlewares() {
        var dispatcher = builder.addMiddlewares(mockMiddleware1, mockMiddleware2).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenMiddlewaresArrayIsNull() {
        assertThatThrownBy(() -> builder.addMiddlewares((HandlerMiddleware[]) null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Middlewares array cannot be null");
    }

    @Test
    void shouldAddMiddlewaresFromList() {
        var middlewares = List.of(mockMiddleware1, mockMiddleware2);
        var dispatcher = builder.addMiddlewares(middlewares).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenMiddlewaresListIsNull() {
        assertThatThrownBy(() -> builder.addMiddlewares((List<HandlerMiddleware>) null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Middlewares list cannot be null");
    }

    @Test
    void shouldAddCommandHandler() {
        var dispatcher = builder.addCommandHandler(TestCommand.class, mockCommandHandler).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenCommandTypeIsNull() {
        assertThatThrownBy(() -> builder.addCommandHandler(null, mockCommandHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Command type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenCommandHandlerIsNull() {
        assertThatThrownBy(() -> builder.addCommandHandler(TestCommand.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Command handler cannot be null");
    }

    @Test
    void shouldAddQueryHandler() {
        var dispatcher = builder.addQueryHandler(TestQuery.class, mockQueryHandler).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenQueryTypeIsNull() {
        assertThatThrownBy(() -> builder.addQueryHandler(null, mockQueryHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Query type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenQueryHandlerIsNull() {
        assertThatThrownBy(() -> builder.addQueryHandler(TestQuery.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Query handler cannot be null");
    }

    @Test
    void shouldAddEventHandler() {
        var dispatcher = builder.addEventHandler(TestEvent.class, mockEventHandler).build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenEventTypeIsNull() {
        assertThatThrownBy(() -> builder.addEventHandler(null, mockEventHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Event type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenEventHandlerIsNull() {
        assertThatThrownBy(() -> builder.addEventHandler(TestEvent.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Event handler cannot be null");
    }

    @Test
    void shouldAddMixedHandlers() {
        var commandRegistration =
                HandlerRegistration.command(TestCommand.class, mockCommandHandler);
        var queryRegistration = HandlerRegistration.query(TestQuery.class, mockQueryHandler);
        var eventRegistration = HandlerRegistration.event(TestEvent.class, mockEventHandler);

        var dispatcher =
                builder.addHandlers(commandRegistration, queryRegistration, eventRegistration)
                        .build();
        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenHandlersArrayIsNull() {
        assertThatThrownBy(() -> builder.addHandlers((HandlerRegistration[]) null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Handlers array cannot be null");
    }

    @Test
    void shouldBuildWithAllConfigurationOptions() {
        var commandRegistration =
                HandlerRegistration.command(TestCommand.class, mockCommandHandler);
        var queryRegistration = HandlerRegistration.query(TestQuery.class, mockQueryHandler);
        var eventRegistration = HandlerRegistration.event(TestEvent.class, mockEventHandler);

        Dispatch4j dispatcher =
                builder.executor(mockExecutor)
                        .addMiddleware(mockMiddleware1)
                        .addMiddleware(mockMiddleware2)
                        .addHandlers(commandRegistration, queryRegistration, eventRegistration)
                        .build();

        assertThat(dispatcher).isNotNull();
    }

    @Test
    void shouldUseStaticBuilderMethod() {
        var dispatcher =
                Dispatch4j.builder()
                        .addCommandHandler(TestCommand.class, mockCommandHandler)
                        .build();
        assertThat(dispatcher).isNotNull();
    }
}
