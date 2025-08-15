package io.github.dispatch4j.exception;

/**
 * Exception thrown when handler validation fails during discovery.
 *
 * <p>This exception is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/exception/HandlerValidationException.java.
 * It is thrown when discovered handlers fail validation checks, such as invalid method signatures,
 * missing annotations, or type mismatches.
 *
 * @see HandlerDiscoveryException
 */
public class HandlerValidationException extends HandlerDiscoveryException {

    private static final long serialVersionUID = 1L;

    private final String validationFailure;

    /**
     * Creates a HandlerValidationException with validation context.
     *
     * @param message the detail message explaining the validation error
     * @param strategyName the name of the strategy performing validation
     * @param source the source object being validated
     * @param validationFailure specific validation rule that failed
     */
    public HandlerValidationException(
            String message, String strategyName, Object source, String validationFailure) {
        super(message, strategyName, source);
        this.validationFailure = validationFailure;
    }

    /**
     * Creates a HandlerValidationException with validation context and cause.
     *
     * @param message the detail message explaining the validation error
     * @param strategyName the name of the strategy performing validation
     * @param source the source object being validated
     * @param validationFailure specific validation rule that failed
     * @param cause the underlying cause of this exception
     */
    public HandlerValidationException(
            String message,
            String strategyName,
            Object source,
            String validationFailure,
            Throwable cause) {
        super(message, strategyName, source, cause);
        this.validationFailure = validationFailure;
    }

    /**
     * Returns the specific validation rule that failed.
     *
     * @return the validation failure description
     */
    public String getValidationFailure() {
        return validationFailure;
    }
}
