package io.github.dispatch4j.core.middleware;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object that carries metadata and state through the middleware chain.
 *
 * <p>The context allows middleware components to share information and communicate with each other
 * during the execution of a handler.
 *
 * <p>Common uses include:
 *
 * <ul>
 *   <li>Storing correlation IDs for distributed tracing
 *   <li>Passing authentication/authorization information
 *   <li>Accumulating metrics and timing information
 *   <li>Sharing computed values between middleware
 * </ul>
 *
 * <p>This class is thread-safe and can be safely used across multiple middleware components.
 */
public final class MiddlewareContext {

    private final MessageType messageType;
    private final Class<?> messageClass;
    private final Map<String, Object> attributes;

    /**
     * Creates a new middleware context.
     *
     * @param messageType the type of message being handled
     * @param messageClass the class of the message
     */
    public MiddlewareContext(MessageType messageType, Class<?> messageClass) {
        this.messageType = messageType;
        this.messageClass = messageClass;
        this.attributes = new HashMap<>();
    }

    /**
     * Gets the type of message being handled.
     *
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Gets the class of the message being handled.
     *
     * @return the message class
     */
    public Class<?> getMessageClass() {
        return messageClass;
    }

    /**
     * Sets an attribute in the context.
     *
     * @param key the attribute key
     * @param value the attribute value
     * @return this context for method chaining
     */
    public MiddlewareContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets an attribute from the context.
     *
     * @param key the attribute key
     * @return an Optional containing the value if present
     */
    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    /**
     * Gets an attribute from the context with a specific type.
     *
     * @param <T> the expected type
     * @param key the attribute key
     * @param type the expected class type
     * @return an Optional containing the typed value if present and of correct type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Removes an attribute from the context.
     *
     * @param key the attribute key
     * @return the removed value, or null if not present
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * Checks if an attribute exists in the context.
     *
     * @param key the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Gets all attributes as an immutable map.
     *
     * @return an immutable copy of all attributes
     */
    public Map<String, Object> getAttributes() {
        return Map.copyOf(attributes);
    }

    /** Enumeration of message types that can be processed. */
    public enum MessageType {
        /** Command message type */
        COMMAND,
        /** Query message type */
        QUERY,
        /** Event message type */
        EVENT
    }
}
