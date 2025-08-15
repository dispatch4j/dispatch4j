package io.github.dispatch4j.exception;

/**
 * Exception thrown when handler registration conflicts occur.
 *
 * <p>This exception is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/exception/HandlerRegistrationConflictException.java.
 * It is thrown when multiple strategies attempt to register handlers for the same message type, or
 * when other registration conflicts are detected during the discovery process.
 *
 * @see HandlerDiscoveryException
 */
public class HandlerRegistrationConflictException extends HandlerDiscoveryException {

    private static final long serialVersionUID = 1L;

    private final Class<?> messageType;
    private final String conflictReason;

    /**
     * Creates a HandlerRegistrationConflictException for the specified message type.
     *
     * @param message the detail message explaining the conflict
     * @param strategyName the name of the strategy that detected the conflict
     * @param source the source object involved in the conflict
     * @param messageType the message type that has conflicting registrations
     * @param conflictReason specific reason for the conflict
     */
    public HandlerRegistrationConflictException(
            String message,
            String strategyName,
            Object source,
            Class<?> messageType,
            String conflictReason) {
        super(message, strategyName, source);
        this.messageType = messageType;
        this.conflictReason = conflictReason;
    }

    /**
     * Returns the message type that has conflicting registrations.
     *
     * @return the message type
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * Returns the specific reason for the registration conflict.
     *
     * @return the conflict reason
     */
    public String getConflictReason() {
        return conflictReason;
    }
}
