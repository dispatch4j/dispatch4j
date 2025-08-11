package io.github.dispatch4j.core.handler;

import io.github.dispatch4j.core.Dispatch4jBuilder;
import io.github.dispatch4j.core.exception.Dispatch4jException;

/**
 * Helper class for registering handlers with their corresponding message types.
 *
 * <p>This class provides a convenient way to pair a handler with its message type for batch
 * registration operations in the {@link Dispatch4jBuilder}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Dispatch4j dispatcher = Dispatch4j.builder()
 *     .addHandlers(
 *         HandlerRegistration.command(CreateUserCommand.class, createUserHandler),
 *         HandlerRegistration.query(GetUserQuery.class, getUserHandler),
 *         HandlerRegistration.event(UserCreatedEvent.class, userCreatedHandler)
 *     )
 *     .build();
 * }</pre>
 */
public final class HandlerRegistration {

    private final Class<?> messageType;
    private final Object handler;

    private HandlerRegistration(Class<?> messageType, Object handler) {
        this.messageType = messageType;
        this.handler = handler;
    }

    /**
     * Creates a command handler registration.
     *
     * @param <C> the command type
     * @param <R> the return type
     * @param commandType the command type (must not be null)
     * @param handler the command handler (must not be null)
     * @return a new handler registration
     * @throws Dispatch4jException if commandType or handler is null
     */
    public static <C, R> HandlerRegistration command(
            Class<C> commandType, io.github.dispatch4j.core.handler.CommandHandler<C, R> handler) {
        if (commandType == null) {
            throw new Dispatch4jException("Command type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Command handler cannot be null");
        }
        return new HandlerRegistration(commandType, handler);
    }

    /**
     * Creates a query handler registration.
     *
     * @param <Q> the query type
     * @param <R> the return type
     * @param queryType the query type (must not be null)
     * @param handler the query handler (must not be null)
     * @return a new handler registration
     * @throws Dispatch4jException if queryType or handler is null
     */
    public static <Q, R> HandlerRegistration query(
            Class<Q> queryType, io.github.dispatch4j.core.handler.QueryHandler<Q, R> handler) {
        if (queryType == null) {
            throw new Dispatch4jException("Query type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Query handler cannot be null");
        }
        return new HandlerRegistration(queryType, handler);
    }

    /**
     * Creates an event handler registration.
     *
     * @param <E> the event type
     * @param eventType the event type (must not be null)
     * @param handler the event handler (must not be null)
     * @return a new handler registration
     * @throws Dispatch4jException if eventType or handler is null
     */
    public static <E> HandlerRegistration event(
            Class<E> eventType, io.github.dispatch4j.core.handler.EventHandler<E> handler) {
        if (eventType == null) {
            throw new Dispatch4jException("Event type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Event handler cannot be null");
        }
        return new HandlerRegistration(eventType, handler);
    }

    /**
     * Returns the message type for this registration.
     *
     * @return the message type
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * Returns the handler for this registration.
     *
     * @return the handler
     */
    public Object getHandler() {
        return handler;
    }
}
