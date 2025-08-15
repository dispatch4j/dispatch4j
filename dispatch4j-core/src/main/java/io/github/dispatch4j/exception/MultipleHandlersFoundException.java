package io.github.dispatch4j.exception;

/**
 * Exception thrown when multiple handlers are registered for the same command or query type.
 *
 * <p>This exception enforces the CQRS constraint that commands and queries can have exactly one
 * handler. It is thrown during handler registration when attempting to register a second handler
 * for a command or query type that already has a handler.
 *
 * <p>This exception is not thrown for events, as events are allowed to have multiple handlers.
 *
 * @see Dispatch4jException
 */
public class MultipleHandlersFoundException extends Dispatch4jException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new MultipleHandlersFoundException with the specified message.
     *
     * @param message the detail message explaining the multiple handlers error
     */
    public MultipleHandlersFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new MultipleHandlersFoundException for the specified message type.
     *
     * @param messageType the message type that has multiple handlers
     * @param handlerCount the number of handlers found for the message type
     */
    public MultipleHandlersFoundException(Class<?> messageType, int handlerCount) {
        super(
                "Multiple handlers found for message type: %s (found %d handlers)"
                        .formatted(messageType.getName(), handlerCount));
    }
}
