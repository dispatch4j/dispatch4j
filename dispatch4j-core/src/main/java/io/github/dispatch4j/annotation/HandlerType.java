package io.github.dispatch4j.annotation;

/**
 * Enumeration representing the different types of handlers in the CQRS architecture.
 *
 * <p>This enum is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/annotation/HandlerType.java. It is used
 * by HandlerRegistration to categorize discovered handlers and ensure proper registration with the
 * appropriate handler registry method.
 *
 * <p>Handler types correspond to the CQRS pattern:
 *
 * <ul>
 *   <li>COMMAND - for command handlers that modify state and return results
 *   <li>QUERY - for query handlers that read state and return results
 *   <li>EVENT - for event handlers that handle notifications and return void
 * </ul>
 */
public enum HandlerType {
    /**
     * Command handler type for handlers that process commands. Commands modify system state and
     * return results.
     */
    COMMAND,

    /**
     * Query handler type for handlers that process queries. Queries read system state and return
     * results.
     */
    QUERY,

    /**
     * Event handler type for handlers that process events. Events represent notifications and
     * handlers return void.
     */
    EVENT
}
