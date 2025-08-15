package io.github.dispatch4j.exception;

/**
 * Exception thrown when generic type resolution fails for interface-based handlers.
 *
 * <p>This exception is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/exception/TypeResolutionException.java.
 * It is thrown by the InterfaceBasedDiscoveryStrategy when it cannot resolve the generic type
 * parameters of handler interfaces, which are needed to determine the message types the handlers
 * can process.
 *
 * @see HandlerDiscoveryException
 */
public class TypeResolutionException extends HandlerDiscoveryException {

    private static final long serialVersionUID = 1L;

    private final Class<?> handlerClass;

    /**
     * Creates a TypeResolutionException for the specified handler class.
     *
     * @param message the detail message explaining the type resolution error
     * @param handlerClass the handler class for which type resolution failed
     */
    public TypeResolutionException(String message, Class<?> handlerClass) {
        super(message, "InterfaceBasedDiscovery", handlerClass);
        this.handlerClass = handlerClass;
    }

    /**
     * Creates a TypeResolutionException with cause for the specified handler class.
     *
     * @param message the detail message explaining the type resolution error
     * @param handlerClass the handler class for which type resolution failed
     * @param cause the underlying cause of this exception
     */
    public TypeResolutionException(String message, Class<?> handlerClass, Throwable cause) {
        super(message, "InterfaceBasedDiscovery", handlerClass, cause);
        this.handlerClass = handlerClass;
    }

    /**
     * Returns the handler class for which type resolution failed.
     *
     * @return the handler class
     */
    public Class<?> getHandlerClass() {
        return handlerClass;
    }
}
