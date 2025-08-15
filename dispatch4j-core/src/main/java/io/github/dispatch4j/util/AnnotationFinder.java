package io.github.dispatch4j.util;

import java.lang.reflect.Method;

/**
 * Abstraction for annotation detection and processing.
 *
 * <p>This interface is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/util/AnnotationUtils.java. It provides a
 * unified way to detect handler annotations that works consistently across different environments
 * (core Java vs. Spring). Different implementations can use different annotation detection
 * strategies while maintaining the same interface contract.
 *
 * @see CoreAnnotationFinder
 */
public interface AnnotationFinder {

    /**
     * Checks if the given method is annotated with @CommandHandler.
     *
     * @param method the method to check
     * @return true if the method has a command handler annotation
     */
    boolean isCommandHandler(Method method);

    /**
     * Checks if the given method is annotated with @QueryHandler.
     *
     * @param method the method to check
     * @return true if the method has a query handler annotation
     */
    boolean isQueryHandler(Method method);

    /**
     * Checks if the given method is annotated with @EventHandler.
     *
     * @param method the method to check
     * @return true if the method has an event handler annotation
     */
    boolean isEventHandler(Method method);

    /**
     * Checks if the given method is annotated with any handler annotation.
     *
     * @param method the method to check
     * @return true if the method has any handler annotation
     */
    default boolean isAnyHandler(Method method) {
        return isCommandHandler(method) || isQueryHandler(method) || isEventHandler(method);
    }

    /**
     * Returns a description of the annotation detection approach used by this implementation. Used
     * for logging and debugging purposes.
     *
     * @return description of the detection approach
     */
    String getDetectionStrategyName();
}
