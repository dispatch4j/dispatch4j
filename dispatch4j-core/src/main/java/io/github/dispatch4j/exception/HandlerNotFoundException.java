package io.github.dispatch4j.exception;

/**
 * Exception thrown when no handler is found for a command or query.
 *
 * <p>This exception is thrown by the dispatcher when attempting to send a command or query for
 * which no handler has been registered. This typically indicates a configuration error where the
 * handler was not properly registered or the message type does not match any registered handlers.
 *
 * <p>Note that this exception is not thrown for events, as events can have zero handlers without
 * being considered an error condition.
 *
 * @see Dispatch4jException
 */
public class HandlerNotFoundException extends Dispatch4jException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new HandlerNotFoundException with the specified message.
     *
     * @param message the detail message explaining which handler was not found
     */
    public HandlerNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new HandlerNotFoundException for the specified message type.
     *
     * @param messageType the message type for which no handler was found
     */
    public HandlerNotFoundException(Class<?> messageType) {
        super("No handler found for message type: %s".formatted(messageType.getName()));
    }
}
