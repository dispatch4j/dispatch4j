package io.github.dispatch4j.core.middleware;

import java.util.ArrayList;
import java.util.Collections;
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

    MiddlewareChain addMiddleware(HandlerMiddleware middleware);

    MiddlewareChain addMiddleware(int index, HandlerMiddleware middleware);

    MiddlewareChain removeMiddleware(HandlerMiddleware middleware);

    <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler);

    /**
     * Creates a builder for constructing middleware chains.
     *
     * @return a new builder instance
     */
    static Builder builder() {
        return new Builder();
    }

    static MiddlewareChain empty() {
        return new NoopMiddlewareChain();
    }

    static MiddlewareChain with(HandlerMiddleware... middlewares) {
        return builder().addAll(middlewares).build();
    }

    /** Builder for constructing middleware chains fluently. */
    class Builder {
        private final List<HandlerMiddleware> middlewares = new ArrayList<>();

        /**
         * Adds a middleware to the chain being built.
         *
         * @param middleware the middleware to add
         * @return this builder for method chaining
         */
        public Builder add(HandlerMiddleware middleware) {
            middlewares.add(middleware);
            return this;
        }

        /**
         * Adds multiple middleware components to the chain being built.
         *
         * @param middlewares the middleware components to add
         * @return this builder for method chaining
         */
        public Builder addAll(HandlerMiddleware... middlewares) {
            Collections.addAll(this.middlewares, middlewares);
            return this;
        }

        /**
         * Adds multiple middleware components from a list to the chain being built.
         *
         * @param middlewares the middleware components to add
         * @return this builder for method chaining
         */
        public Builder addAll(List<HandlerMiddleware> middlewares) {
            this.middlewares.addAll(middlewares);
            return this;
        }

        /**
         * Builds the middleware chain.
         *
         * @return the constructed middleware chain
         */
        public MiddlewareChain build() {
            if (middlewares.isEmpty()) {
                return new NoopMiddlewareChain();
            }
            return new DefaultMiddlewareChain(middlewares);
        }
    }
}
