package io.github.dispatch4j.core.middleware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builder for constructing middleware chains fluently. */
public class MiddlewareChainBuilder {
    private final List<HandlerMiddleware> middlewares = new ArrayList<>();

    MiddlewareChainBuilder() {
        // Private constructor to prevent instantiation from outside
    }

    /**
     * Adds a middleware to the chain being built.
     *
     * @param middleware the middleware to add
     * @return this builder for method chaining
     */
    public MiddlewareChainBuilder add(HandlerMiddleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    /**
     * Adds multiple middleware components to the chain being built.
     *
     * @param middlewares the middleware components to add
     * @return this builder for method chaining
     */
    public MiddlewareChainBuilder addAll(HandlerMiddleware... middlewares) {
        Collections.addAll(this.middlewares, middlewares);
        return this;
    }

    /**
     * Adds multiple middleware components from a list to the chain being built.
     *
     * @param middlewares the middleware components to add
     * @return this builder for method chaining
     */
    public MiddlewareChainBuilder addAll(List<HandlerMiddleware> middlewares) {
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
