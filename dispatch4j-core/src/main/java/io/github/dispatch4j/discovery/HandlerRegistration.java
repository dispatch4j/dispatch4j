package io.github.dispatch4j.discovery;

import io.github.dispatch4j.annotation.HandlerType;
import io.github.dispatch4j.exception.Dispatch4jException;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.EventHandler;
import io.github.dispatch4j.handler.QueryHandler;

/**
 * Immutable record representing a discovered handler registration.
 *
 * <p>This record is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/strategy/HandlerRegistration.java. It is
 * used for tracking and validation of discovered handlers by HandlerDiscoveryStrategy
 * implementations. This is separate from the builder pattern HandlerRegistration class.
 *
 * <p>The handlerInstance is type-safe - it will be a CommandHandler&lt;T, ?&gt;, QueryHandler&lt;T,
 * ?&gt;, or EventHandler&lt;T&gt; based on the handlerKind.
 */
public record HandlerRegistration(
        Class<?> messageType,
        Class<?> handlerType,
        HandlerType handlerKind,
        String handlerName,
        Object handlerInstance, // Actual type: CommandHandler<T, ?>, QueryHandler<T, ?>, or
        // EventHandler<T>
        String discoverySource) {

    public HandlerRegistration {
        if (messageType == null) {
            throw new Dispatch4jException("Message type cannot be null");
        }
        if (handlerType == null) {
            throw new Dispatch4jException("Handler type cannot be null");
        }
        if (handlerKind == null) {
            throw new Dispatch4jException("Handler kind cannot be null");
        }
        if (handlerName == null) {
            throw new Dispatch4jException("Handler name cannot be null");
        }
        if (handlerInstance == null) {
            throw new Dispatch4jException("Handler instance cannot be null");
        }
        if (discoverySource == null) {
            throw new Dispatch4jException("Discovery source cannot be null");
        }
    }

    /**
     * Creates a handler registration with the specified handler kind.
     *
     * @param messageType the message type
     * @param handlerType the handler class type
     * @param handlerKind the type of handler (COMMAND, QUERY, or EVENT)
     * @param instance the handler instance
     * @param name the handler name
     * @param source the discovery source
     * @return handler registration
     */
    private static <T> HandlerRegistration of(
            Class<T> messageType,
            Class<?> handlerType,
            HandlerType handlerKind,
            Object instance,
            String name,
            String source) {
        return new HandlerRegistration(
                messageType, handlerType, handlerKind, name, instance, source);
    }

    /**
     * Creates a command handler registration with type-safe handler instance.
     *
     * @param messageType the command message type
     * @param handlerType the handler class type
     * @param instance the handler instance (must be CommandHandler&lt;T, ?&gt;)
     * @param name the handler name
     * @param source the discovery source
     * @return command handler registration
     */
    public static <T> HandlerRegistration commandHandler(
            Class<T> messageType,
            Class<?> handlerType,
            CommandHandler<T, ?> instance,
            String name,
            String source) {
        return of(messageType, handlerType, HandlerType.COMMAND, instance, name, source);
    }

    /**
     * Creates a query handler registration with type-safe handler instance.
     *
     * @param messageType the query message type
     * @param handlerType the handler class type
     * @param instance the handler instance (must be QueryHandler&lt;T, ?&gt;)
     * @param name the handler name
     * @param source the discovery source
     * @return query handler registration
     */
    public static <T> HandlerRegistration queryHandler(
            Class<T> messageType,
            Class<?> handlerType,
            QueryHandler<T, ?> instance,
            String name,
            String source) {
        return of(messageType, handlerType, HandlerType.QUERY, instance, name, source);
    }

    /**
     * Creates an event handler registration with type-safe handler instance.
     *
     * @param messageType the event message type
     * @param handlerType the handler class type
     * @param instance the handler instance (must be EventHandler&lt;T&gt;)
     * @param name the handler name
     * @param source the discovery source
     * @return event handler registration
     */
    public static <T> HandlerRegistration eventHandler(
            Class<T> messageType,
            Class<?> handlerType,
            EventHandler<T> instance,
            String name,
            String source) {
        return of(messageType, handlerType, HandlerType.EVENT, instance, name, source);
    }
}
