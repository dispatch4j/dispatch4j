package io.github.dispatch4j.middleware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Default implementation of a middleware chain that contains one or more middleware components.
 *
 * <p>This implementation executes middleware components in the order they were added to the chain,
 * using a recursive chain-of-responsibility pattern. Each middleware can perform pre-processing,
 * delegate to the next middleware or handler, and perform post-processing.
 *
 * <p>The chain is built recursively during execution, creating a nested function structure where
 * each middleware wraps the next one in the chain. This approach provides clean separation of
 * concerns and allows middleware to control the execution flow.
 *
 * <p>This class is immutable after construction and thread-safe. The internal list is copied during
 * construction to prevent external modifications.
 *
 * @see NoopMiddlewareChain for the optimized empty chain implementation
 */
final class DefaultMiddlewareChain implements MiddlewareChain {

    private final List<HandlerMiddleware> middlewares;

    /**
     * Creates a new middleware chain with the given middleware components. The provided list is
     * defensively copied to ensure immutability.
     *
     * @param middlewares the initial middleware components to add to the chain
     */
    DefaultMiddlewareChain(List<HandlerMiddleware> middlewares) {
        this.middlewares = new ArrayList<>(middlewares);
    }

    /**
     * Gets an immutable copy of the middlewares in this chain. This method is used by the mutator
     * to copy existing middlewares when creating modified chains.
     *
     * @return immutable list of middlewares in execution order
     */
    @Override
    public List<HandlerMiddleware> getMiddlewares() {
        return List.copyOf(middlewares);
    }

    /**
     * Gets the number of middleware components in the chain.
     *
     * @return the number of middleware components (always greater than 0 for this implementation)
     */
    @Override
    public int size() {
        return middlewares.size();
    }

    /**
     * Checks if the middleware chain is empty. For this implementation, this method always returns
     * false since empty chains use {@link NoopMiddlewareChain} for performance optimization.
     *
     * @return false (this implementation never contains empty chains)
     */
    @Override
    public boolean isEmpty() {
        return middlewares.isEmpty();
    }

    /**
     * Executes the middleware chain for a given message.
     *
     * <p>This method builds the complete middleware chain recursively and then executes it. Each
     * middleware in the chain can perform pre-processing, delegate to the next middleware, and
     * perform post-processing.
     *
     * <p>The execution flow is:
     *
     * <ol>
     *   <li>Build the complete chain as nested functions
     *   <li>Execute the outermost middleware
     *   <li>Each middleware decides whether to continue to the next one
     *   <li>Eventually reach the final handler or short-circuit
     * </ol>
     *
     * @param <T> the message type
     * @param <R> the return type
     * @param message the message to process through the middleware chain
     * @param context the middleware context containing request metadata
     * @param finalHandler the final handler to execute after all middleware processing
     * @return the result of the middleware chain execution
     */
    @Override
    public <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler) {
        return buildChain(finalHandler, context, 0).apply(message);
    }

    /**
     * Recursively builds the middleware execution chain as nested functions.
     *
     * <p>This method creates a nested function structure where each middleware wraps the next one
     * in the chain. The recursion terminates when all middlewares have been processed, returning
     * the final handler.
     *
     * <p>Example chain: [M1, M2, M3] becomes: {@code M1(message, context, () -> M2(message,
     * context, () -> M3(message, context, finalHandler)))}
     *
     * @param <T> the message type
     * @param <R> the return type
     * @param finalHandler the final handler to execute at the end of the chain
     * @param context the middleware context shared across all middleware
     * @param index the current middleware index in the chain
     * @return a function representing the remaining chain from the current index
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
