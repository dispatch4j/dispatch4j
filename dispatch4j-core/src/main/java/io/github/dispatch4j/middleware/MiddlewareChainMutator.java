package io.github.dispatch4j.middleware;

import io.github.dispatch4j.exception.Dispatch4jException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutator for creating modified copies of middleware chains.
 *
 * <p>This class allow modification of immutable middleware chains by building a new chain with the
 * desired changes.
 */
public class MiddlewareChainMutator {

    private final List<HandlerMiddleware> middlewares = new ArrayList<>();

    MiddlewareChainMutator(MiddlewareChain original) {
        middlewares.addAll(original.getMiddlewares());
    }

    /**
     * Adds a middleware to the end of the chain.
     *
     * @param middleware the middleware to add
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator add(HandlerMiddleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    /**
     * Adds multiple middleware components to the chain.
     *
     * @param middlewares the middleware components to add
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator addAll(HandlerMiddleware... middlewares) {
        this.middlewares.addAll(List.of(middlewares));
        return this;
    }

    /**
     * Adds multiple middleware components from a list to the chain.
     *
     * @param middlewares the middleware components to add
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator addAll(List<HandlerMiddleware> middlewares) {
        this.middlewares.addAll(middlewares);
        return this;
    }

    /**
     * Removes a specific middleware from the chain.
     *
     * @param middleware the middleware to remove
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator remove(HandlerMiddleware middleware) {
        middlewares.remove(middleware);
        return this;
    }

    /**
     * Removes all middleware instances of the specified class from the chain.
     *
     * @param middlewareClass the class of middleware to remove
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator remove(Class<? extends HandlerMiddleware> middlewareClass) {
        middlewares.removeIf(middleware -> middleware.getClass().equals(middlewareClass));
        return this;
    }

    /**
     * Removes a middleware at the specified index from the chain.
     *
     * @param index the index of the middleware to remove
     * @return this mutator for method chaining
     * @throws Dispatch4jException if the index is out of bounds
     */
    public MiddlewareChainMutator removeAt(int index) {
        if (index < 0 || index >= middlewares.size()) {
            throw new Dispatch4jException("Index out of bounds: " + index);
        }
        middlewares.remove(index);
        return this;
    }

    /**
     * Removes all middleware from the chain.
     *
     * @return this mutator for method chaining
     */
    public MiddlewareChainMutator clear() {
        middlewares.clear();
        return this;
    }

    /**
     * Builds the mutated middleware chain.
     *
     * @return the new middleware chain with applied mutations
     */
    public MiddlewareChain build() {
        return MiddlewareChain.builder().addAll(middlewares).build();
    }
}
