package io.github.dispatch4j.middleware;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Middleware that logs handler execution details including timing information.
 *
 * <p>This middleware logs:
 *
 * <ul>
 *   <li>Start of handler execution with message details
 *   <li>Successful completion with execution time
 *   <li>Exceptions with error details
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var dispatcher = new Dispatch4j()
 *     .addMiddleware(new LoggingMiddleware());
 * }</pre>
 */
public final class LoggingMiddleware implements HandlerMiddleware {

    private static final Logger log = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public <T, R> R handle(T message, MiddlewareContext context, Next<T, R> next) {
        var messageType = context.getMessageType();
        var messageClass = context.getMessageClass().getSimpleName();
        var start = Instant.now();

        log.debug("Starting {} handler for: {}", messageType, messageClass);

        try {
            return next.handle(message);
        } finally {
            var duration = Duration.between(start, Instant.now());
            log.debug(
                    "Completed {} handler for: {} in {}ms",
                    messageType,
                    messageClass,
                    duration.toMillis());
        }
    }
}
