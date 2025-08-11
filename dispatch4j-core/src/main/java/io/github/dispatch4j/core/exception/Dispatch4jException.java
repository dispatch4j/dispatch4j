package io.github.dispatch4j.core.exception;

/**
 * Base exception class for all Dispatch4j-related errors.
 *
 * <p>This runtime exception is the parent class for all exceptions thrown by the Dispatch4j
 * library. It extends {@link RuntimeException} to align with modern Java practices and avoid
 * forcing callers to handle checked exceptions for framework-level errors.
 *
 * <p>Common scenarios where this exception is thrown:
 *
 * <ul>
 *   <li>Null parameter validation failures
 *   <li>Invalid configuration or setup errors
 *   <li>Handler registration constraints violations
 * </ul>
 *
 * @see HandlerNotFoundException
 * @see MultipleHandlersFoundException
 */
public class Dispatch4jException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Dispatch4jException with the specified message.
     *
     * @param message the detail message explaining the error
     */
    public Dispatch4jException(String message) {
        super(message);
    }

    /**
     * Creates a new Dispatch4jException with the specified message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause the underlying cause of this exception
     */
    public Dispatch4jException(String message, Throwable cause) {
        super(message, cause);
    }
}
