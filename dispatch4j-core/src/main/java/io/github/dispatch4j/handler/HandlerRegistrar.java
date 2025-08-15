package io.github.dispatch4j.handler;

import io.github.dispatch4j.Dispatch4j;

/**
 * Interface for registering command, query, and event handlers.
 *
 * <p>This interface provides methods to register handlers for different message types.
 * Implementations should enforce CQRS constraints where commands and queries can have exactly one
 * handler, while events can have multiple handlers.
 *
 * @see HandlerRegistry
 * @see Dispatch4j
 */
public interface HandlerRegistrar {

    /**
     * Registers a command handler for the specified command type.
     *
     * @param commandType the command type to register the handler for
     * @param handler the command handler to register
     */
    <T> void registerCommandHandler(Class<T> commandType, CommandHandler<T, ?> handler);

    /**
     * Registers a query handler for the specified query type.
     *
     * @param queryType the query type to register the handler for
     * @param handler the query handler to register
     */
    <T> void registerQueryHandler(Class<T> queryType, QueryHandler<T, ?> handler);

    /**
     * Registers an event handler for the specified event type.
     *
     * @param eventType the event type to register the handler for
     * @param handler the event handler to register
     */
    <T> void registerEventHandler(Class<T> eventType, EventHandler<T> handler);

    void registerHandlersIn(Object handler);
}
