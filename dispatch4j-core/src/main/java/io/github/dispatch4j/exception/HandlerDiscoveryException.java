package io.github.dispatch4j.exception;

/**
 * Exception thrown when handler discovery fails.
 *
 * <p>This exception is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/exception/HandlerDiscoveryException.java.
 * It is thrown during the handler discovery process when strategies encounter errors while scanning
 * for and registering handlers. It provides context about which strategy failed and what source was
 * being processed.
 *
 * @see Dispatch4jException
 */
public class HandlerDiscoveryException extends Dispatch4jException {

    private static final long serialVersionUID = 1L;

    private final String strategyName;
    private final transient Object source;

    /**
     * Creates a HandlerDiscoveryException with message, strategy, and source context.
     *
     * @param message the detail message explaining the error
     * @param strategyName the name of the strategy that failed
     * @param source the source object being processed when the error occurred
     */
    public HandlerDiscoveryException(String message, String strategyName, Object source) {
        super(message);
        this.strategyName = strategyName;
        this.source = source;
    }

    /**
     * Creates a HandlerDiscoveryException with message, strategy, source, and cause.
     *
     * @param message the detail message explaining the error
     * @param strategyName the name of the strategy that failed
     * @param source the source object being processed when the error occurred
     * @param cause the underlying cause of this exception
     */
    public HandlerDiscoveryException(
            String message, String strategyName, Object source, Throwable cause) {
        super(message, cause);
        this.strategyName = strategyName;
        this.source = source;
    }

    /**
     * Returns the name of the strategy that encountered the error.
     *
     * @return the strategy name
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Returns the source object being processed when the error occurred.
     *
     * @return the source object
     */
    public Object getSource() {
        return source;
    }
}
