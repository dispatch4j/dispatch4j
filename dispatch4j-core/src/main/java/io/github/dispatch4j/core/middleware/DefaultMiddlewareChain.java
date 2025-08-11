package io.github.dispatch4j.core.middleware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class DefaultMiddlewareChain implements MiddlewareChain {

    private final List<HandlerMiddleware> middlewares;

    /**
     * Creates a new middleware chain with the given middleware components.
     *
     * @param middlewares the initial middleware components
     */
    public DefaultMiddlewareChain(List<HandlerMiddleware> middlewares) {
        this.middlewares = new ArrayList<>(middlewares);
    }

    /**
     * Adds a middleware to the end of the chain.
     *
     * @param middleware the middleware to add
     * @return this chain for method chaining
     */
    @Override
    public MiddlewareChain addMiddleware(HandlerMiddleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    /**
     * Adds a middleware at the specified position in the chain.
     *
     * @param index the position to insert the middleware
     * @param middleware the middleware to add
     * @return this chain for method chaining
     */
    @Override
    public MiddlewareChain addMiddleware(int index, HandlerMiddleware middleware) {
        middlewares.add(index, middleware);
        return this;
    }

    /**
     * Removes a middleware from the chain.
     *
     * @param middleware the middleware to remove
     * @return true if the middleware was removed
     */
    @Override
    public MiddlewareChain removeMiddleware(HandlerMiddleware middleware) {
        middlewares.remove(middleware);
        if (middlewares.isEmpty()) {
            return new NoopMiddlewareChain();
        }
        return this;
    }

    /**
     * Gets the number of middleware components in the chain.
     *
     * @return the number of middleware components
     */
    @Override
    public int size() {
        return middlewares.size();
    }

    /**
     * Checks if the middleware chain is empty.
     *
     * @return true if there are no middleware components
     */
    @Override
    public boolean isEmpty() {
        return middlewares.isEmpty();
    }

    /**
     * Executes the middleware chain for a given message.
     *
     * @param <T> the message type
     * @param <R> the return type
     * @param message the message to process
     * @param context the middleware context
     * @param finalHandler the final handler to execute after all middleware
     * @return the result of the execution
     */
    @Override
    public <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler) {
        return buildChain(finalHandler, context, 0).apply(message);
    }

    /**
     * Builds the middleware chain recursively.
     *
     * @param <T> the message type
     * @param <R> the return type
     * @param finalHandler the final handler
     * @param context the middleware context
     * @param index the current middleware index
     * @return a function representing the chain
     */
    private <T, R> Function<T, R> buildChain(
            Function<T, R> finalHandler, MiddlewareContext context, int index) {

        if (index >= middlewares.size()) {
            return finalHandler;
        }

        var current = middlewares.get(index);
        var next = buildChain(finalHandler, context, index + 1);

        return message -> current.handle(message, context, next::apply);
    }
}
