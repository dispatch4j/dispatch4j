package io.github.dispatch4j.middleware;

import java.util.List;
import java.util.function.Function;

/**
 * A no-operation middleware chain implementation that contains no middleware components.
 *
 * <p>This implementation is used as an optimization when a middleware chain is empty, avoiding the
 * overhead of checking for an empty list and building a chain. It directly delegates to the final
 * handler without any middleware processing.
 */
class NoopMiddlewareChain implements MiddlewareChain {
    /**
     * Returns the size of this middleware chain, which is always 0.
     *
     * @return 0
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Checks if this middleware chain is empty, which is always true.
     *
     * @return true
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Returns an empty list of middlewares.
     *
     * @return empty immutable list
     */
    @Override
    public List<HandlerMiddleware> getMiddlewares() {
        return List.of();
    }

    /**
     * Executes the final handler directly without any middleware processing. This provides optimal
     * performance for empty middleware chains by skipping all middleware chain building and
     * iteration overhead.
     *
     * @param <T> the message type
     * @param <R> the return type
     * @param message the message to process (unused in this implementation)
     * @param context the middleware context (unused in this implementation)
     * @param finalHandler the final handler to execute
     * @return the result of the final handler execution
     */
    @Override
    public <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler) {
        return finalHandler.apply(message);
    }
}
