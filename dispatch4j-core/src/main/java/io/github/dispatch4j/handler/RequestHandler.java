package io.github.dispatch4j.handler;

/**
 * Base interface for request-response handlers that return a value.
 *
 * <p>This interface provides a common contract for handlers that process input messages and return
 * results. It is extended by {@link CommandHandler} and {@link QueryHandler} to provide specific
 * semantics for commands and queries.
 *
 * <p>This is a functional interface that can be implemented as a lambda expression or method
 * reference.
 *
 * @param <T> the input message type
 * @param <R> the return type
 * @see CommandHandler
 * @see QueryHandler
 */
@FunctionalInterface
public interface RequestHandler<T, R> {

    /**
     * Processes the input message and returns a result.
     *
     * @param input the input message to process
     * @return the result of processing the input
     * @throws RuntimeException if an error occurs during processing
     */
    R handle(T input);
}
