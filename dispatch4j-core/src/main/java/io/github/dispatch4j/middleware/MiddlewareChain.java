package io.github.dispatch4j.middleware;

import java.util.List;
import java.util.function.Function;

/**
 * Manages and executes a chain of middleware components.
 *
 * <p>The chain executes middleware in the order they were added, with each middleware having the
 * opportunity to process the message before and/or after the next element in the chain.
 *
 * <p>This class is thread-safe and can be shared across multiple threads.
 */
public interface MiddlewareChain {
    int size();

    boolean isEmpty();

    default MiddlewareChainMutator mutate() {
        return new MiddlewareChainMutator(this);
    }

    List<HandlerMiddleware> getMiddlewares();

    <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler);

    /**
     * Creates a builder for constructing middleware chains.
     *
     * @return a new builder instance
     */
    static MiddlewareChainBuilder builder() {
        return new MiddlewareChainBuilder();
    }

    static MiddlewareChain empty() {
        return builder().build();
    }

    static MiddlewareChain with(HandlerMiddleware... middlewares) {
        return builder().addAll(middlewares).build();
    }
}
