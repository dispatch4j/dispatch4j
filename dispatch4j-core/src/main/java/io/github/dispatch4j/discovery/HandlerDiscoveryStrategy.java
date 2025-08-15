package io.github.dispatch4j.discovery;

import java.util.Collection;

/**
 * Strategy interface for discovering message handlers from source objects.
 *
 * <p>This interface is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/strategy/HandlerDiscoveryStrategy.java.
 * It focuses solely on discovery - finding handlers and extracting their metadata. Registration
 * with a HandlerRegistry is performed separately by the caller, maintaining clear separation of
 * concerns.
 *
 * <p>This interface is used by HandlerRegistry.registerHandlersIn() as the single entry point for
 * all handler discovery. Implementations define different approaches for finding handlers in source
 * objects (beans, classes, packages) and returning their registration metadata.
 *
 * @see AnnotationBasedDiscoveryStrategy
 * @see InterfaceBasedDiscoveryStrategy
 * @see CompositeDiscoveryStrategy
 */
public interface HandlerDiscoveryStrategy {

    /**
     * Discovers handlers from the given source object.
     *
     * <p>This method only performs discovery and validation - it does NOT register handlers with
     * any registry. The caller is responsible for using the returned HandlerRegistration objects to
     * perform actual registration.
     *
     * @param source the source object to scan for handlers (bean, class, etc.)
     * @return collection of discovered handler registrations metadata
     * @throws io.github.dispatch4j.exception.HandlerDiscoveryException if discovery or validation
     *     fails
     */
    Collection<HandlerRegistration> discoverHandlers(Object source);

    /**
     * Returns true if this strategy can handle the given source type.
     *
     * @param source the source object to evaluate
     * @return true if this strategy supports the source type
     */
    boolean supports(Object source);

    /**
     * Returns the priority of this strategy (higher values = higher priority). Used by
     * CompositeDiscoveryStrategy for ordering execution.
     *
     * @return strategy priority, default is 0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns a human-readable name for this strategy. Used for logging and debugging purposes.
     *
     * @return strategy name
     */
    String getName();
}
