package io.github.dispatch4j.core;

import io.github.dispatch4j.core.exception.Dispatch4jException;
import io.github.dispatch4j.core.handler.*;
import io.github.dispatch4j.core.middleware.HandlerMiddleware;
import io.github.dispatch4j.core.middleware.MiddlewareChain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Builder for creating and configuring Dispatch4j instances with fluent API.
 *
 * <p>This builder provides a convenient way to construct Dispatch4j instances with custom
 * configuration including:
 *
 * <ul>
 *   <li>Custom executor for async operations
 *   <li>Middleware components for cross-cutting concerns
 *   <li>Command, query, and event handlers
 *   <li>Custom handler registry
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Dispatch4j dispatcher = Dispatch4j.builder()
 *     .addMiddleware(new LoggingMiddleware())
 *     .addCommandHandler(CreateUserCommand.class, createUserHandler)
 *     .addQueryHandler(GetUserQuery.class, getUserHandler)
 *     .addEventHandler(UserCreatedEvent.class, userCreatedHandler)
 *     .executor(customExecutor)
 *     .build();
 * }</pre>
 *
 * <p>This class is not thread-safe and should not be shared across multiple threads during the
 * building process.
 */
public final class Dispatch4jBuilder {

    private HandlerRegistry handlerRegistry;
    private Executor executor;
    private final List<HandlerMiddleware> middlewares;

    /** Creates a new builder with default configuration. */
    public Dispatch4jBuilder() {
        this.handlerRegistry = new HandlerRegistry();
        this.executor = ForkJoinPool.commonPool();
        this.middlewares = new ArrayList<>();
    }

    /**
     * Sets the executor to use for asynchronous operations.
     *
     * @param executor the executor to use (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if executor is null
     */
    public Dispatch4jBuilder executor(Executor executor) {
        if (executor == null) {
            throw new Dispatch4jException("Executor cannot be null");
        }
        this.executor = executor;
        return this;
    }

    /**
     * Sets a custom handler registry.
     *
     * @param handlerRegistry the handler registry to use (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if handlerRegistry is null
     */
    public Dispatch4jBuilder handlerRegistry(HandlerRegistry handlerRegistry) {
        if (handlerRegistry == null) {
            throw new Dispatch4jException("Handler registry cannot be null");
        }
        this.handlerRegistry = handlerRegistry;
        return this;
    }

    /**
     * Adds a middleware component to the middleware chain.
     *
     * <p>Middleware components are executed in the order they are added.
     *
     * @param middleware the middleware to add (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if middleware is null
     */
    public Dispatch4jBuilder addMiddleware(HandlerMiddleware middleware) {
        if (middleware == null) {
            throw new Dispatch4jException("Middleware cannot be null");
        }
        this.middlewares.add(middleware);
        return this;
    }

    /**
     * Adds multiple middleware components to the middleware chain.
     *
     * <p>Middleware components are executed in the order they are provided.
     *
     * @param middlewares the middleware components to add (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if middlewares array is null or contains null elements
     */
    public Dispatch4jBuilder addMiddlewares(HandlerMiddleware... middlewares) {
        if (middlewares == null) {
            throw new Dispatch4jException("Middlewares array cannot be null");
        }
        Arrays.stream(middlewares)
                .forEach(this::addMiddleware);
        return this;
    }

    /**
     * Adds multiple middleware components from a list to the middleware chain.
     *
     * @param middlewares the middleware components to add (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if middlewares list is null or contains null elements
     */
    public Dispatch4jBuilder addMiddlewares(List<HandlerMiddleware> middlewares) {
        if (middlewares == null) {
            throw new Dispatch4jException("Middlewares list cannot be null");
        }
        middlewares.forEach(this::addMiddleware);
        return this;
    }

    /**
     * Registers a command handler for the specified command type.
     *
     * @param <C> the command type
     * @param <R> the return type
     * @param commandType the command type to register the handler for (must not be null)
     * @param handler the command handler to register (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if commandType or handler is null
     */
    public <C, R> Dispatch4jBuilder addCommandHandler(
            Class<C> commandType, CommandHandler<C, R> handler) {
        if (commandType == null) {
            throw new Dispatch4jException("Command type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Command handler cannot be null");
        }
        this.handlerRegistry.registerCommandHandler(commandType, handler);
        return this;
    }

    /**
     * Registers a query handler for the specified query type.
     *
     * @param <Q> the query type
     * @param <R> the return type
     * @param queryType the query type to register the handler for (must not be null)
     * @param handler the query handler to register (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if queryType or handler is null
     */
    public <Q, R> Dispatch4jBuilder addQueryHandler(
            Class<Q> queryType, QueryHandler<Q, R> handler) {
        if (queryType == null) {
            throw new Dispatch4jException("Query type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Query handler cannot be null");
        }
        this.handlerRegistry.registerQueryHandler(queryType, handler);
        return this;
    }

    /**
     * Registers an event handler for the specified event type.
     *
     * @param <E> the event type
     * @param eventType the event type to register the handler for (must not be null)
     * @param handler the event handler to register (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if eventType or handler is null
     */
    public <E> Dispatch4jBuilder addEventHandler(Class<E> eventType, EventHandler<E> handler) {
        if (eventType == null) {
            throw new Dispatch4jException("Event type cannot be null");
        }
        if (handler == null) {
            throw new Dispatch4jException("Event handler cannot be null");
        }
        this.handlerRegistry.registerEventHandler(eventType, handler);
        return this;
    }

    /**
     * Registers multiple handlers of any type.
     *
     * @param handlers the handlers to register (must not be null)
     * @return this builder for method chaining
     * @throws Dispatch4jException if handlers is null or contains unsupported handler types
     */
    public Dispatch4jBuilder addHandlers(HandlerRegistration... handlers) {
        if (handlers == null) {
            throw new Dispatch4jException("Handlers array cannot be null");
        }
        for (HandlerRegistration registration : handlers) {
            if (registration == null) {
                throw new Dispatch4jException("Handler registration cannot be null");
            }
            var handler = registration.getHandler();
            var messageType = registration.getMessageType();

            switch (handler) {
                case CommandHandler<?, ?> commandHandler ->
                        this.handlerRegistry.registerCommandHandler(messageType, commandHandler);
                case QueryHandler<?, ?> queryHandler ->
                        this.handlerRegistry.registerQueryHandler(messageType, queryHandler);
                case EventHandler<?> eventHandler ->
                        this.handlerRegistry.registerEventHandler(messageType, eventHandler);
                default ->
                        throw new Dispatch4jException(
                                "Unsupported handler type: " + handler.getClass());
            }
        }
        return this;
    }

    /**
     * Builds the Dispatch4j instance with the configured settings.
     *
     * @return a new Dispatch4j instance
     */
    public Dispatch4j build() {
        var chain = MiddlewareChain.builder().addAll(middlewares).build();
        return new Dispatch4j(handlerRegistry, executor, chain);
    }
}
