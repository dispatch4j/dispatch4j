package io.github.dispatch4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dispatch4j.core.exception.Dispatch4jException;
import io.github.dispatch4j.core.handler.CommandHandler;
import io.github.dispatch4j.core.handler.EventHandler;
import io.github.dispatch4j.core.handler.HandlerRegistration;
import io.github.dispatch4j.core.handler.QueryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class HandlerRegistrationTest {

    @Mock private CommandHandler<TestCommand, String> commandHandler;

    @Mock private QueryHandler<TestQuery, String> queryHandler;

    @Mock private EventHandler<TestEvent> eventHandler;

    static class TestCommand {}

    static class TestQuery {}

    static class TestEvent {}

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateCommandRegistration() {
        var registration = HandlerRegistration.command(TestCommand.class, commandHandler);

        assertThat(registration.getMessageType()).isEqualTo(TestCommand.class);
        assertThat(registration.getHandler()).isEqualTo(commandHandler);
    }

    @Test
    void shouldThrowExceptionWhenCommandTypeIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.command(null, commandHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Command type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenCommandHandlerIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.command(TestCommand.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Command handler cannot be null");
    }

    @Test
    void shouldCreateQueryRegistration() {
        var registration = HandlerRegistration.query(TestQuery.class, queryHandler);

        assertThat(registration.getMessageType()).isEqualTo(TestQuery.class);
        assertThat(registration.getHandler()).isEqualTo(queryHandler);
    }

    @Test
    void shouldThrowExceptionWhenQueryTypeIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.query(null, queryHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Query type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenQueryHandlerIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.query(TestQuery.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Query handler cannot be null");
    }

    @Test
    void shouldCreateEventRegistration() {
        var registration = HandlerRegistration.event(TestEvent.class, eventHandler);

        assertThat(registration.getMessageType()).isEqualTo(TestEvent.class);
        assertThat(registration.getHandler()).isEqualTo(eventHandler);
    }

    @Test
    void shouldThrowExceptionWhenEventTypeIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.event(null, eventHandler))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Event type cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenEventHandlerIsNull() {
        assertThatThrownBy(() -> HandlerRegistration.event(TestEvent.class, null))
                .isInstanceOf(Dispatch4jException.class)
                .hasMessage("Event handler cannot be null");
    }
}
