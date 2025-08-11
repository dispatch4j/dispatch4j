package io.github.dispatch4j.core.middleware;

/**
 * Middleware interface for intercepting and customizing handler behavior.
 *
 * <p>Middleware allows you to add cross-cutting concerns to handler execution such as:
 *
 * <ul>
 *   <li>Logging and monitoring
 *   <li>Authentication and authorization
 *   <li>Validation
 *   <li>Error handling and retry logic
 *   <li>Caching
 *   <li>Performance tracking
 * </ul>
 *
 * <p>Middleware components are executed in a chain pattern, where each middleware can perform
 * operations before and/or after the next middleware or handler.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * public class LoggingMiddleware implements HandlerMiddleware {
 *     @Override
 *     public <T, R> R handle(T message, MiddlewareContext context, Next<T, R> next) {
 *         logger.info("Before handling: {}", message);
 *         try {
 *             R result = next.handle(message);
 *             logger.info("After handling: {}", result);
 *             return result;
 *         } catch (Exception e) {
 *             logger.error("Error handling: {}", message, e);
 *             throw e;
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see MiddlewareContext
 * @see Next
 */
@FunctionalInterface
public interface HandlerMiddleware {

    /**
     * Handles the message by optionally performing pre/post processing and delegating to the next
     * middleware or handler.
     *
     * @param <T> the type of the message being handled
     * @param <R> the return type of the handler
     * @param message the message being processed
     * @param context the middleware context containing metadata
     * @param next the next middleware or handler in the chain
     * @return the result of the handler execution
     * @throws RuntimeException if an error occurs during processing
     */
    <T, R> R handle(T message, MiddlewareContext context, Next<T, R> next);

    /**
     * Functional interface representing the next element in the middleware chain. This can be
     * another middleware or the final handler.
     *
     * @param <T> the type of the message
     * @param <R> the return type
     */
    @FunctionalInterface
    interface Next<T, R> {
        /**
         * Invokes the next middleware or handler in the chain.
         *
         * @param message the message to process
         * @return the result of processing
         */
        R handle(T message);
    }
}
