package io.github.dispatch4j.discovery;

import io.github.dispatch4j.exception.HandlerDiscoveryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite strategy that combines multiple discovery strategies.
 *
 * <p>This strategy is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/strategy/CompositeDiscoveryStrategy.java.
 * It executes strategies in priority order and handles conflicts between strategies. Provides
 * unified discovery interface while supporting multiple discovery approaches simultaneously.
 */
public class CompositeDiscoveryStrategy implements HandlerDiscoveryStrategy {

    private static final Logger log = LoggerFactory.getLogger(CompositeDiscoveryStrategy.class);
    private final List<HandlerDiscoveryStrategy> strategies;
    private final ConflictResolutionStrategy conflictResolver;

    public CompositeDiscoveryStrategy(List<HandlerDiscoveryStrategy> strategies) {
        this(strategies, ConflictResolutionStrategy.FIRST_WINS);
    }

    public CompositeDiscoveryStrategy(
            List<HandlerDiscoveryStrategy> strategies,
            ConflictResolutionStrategy conflictResolver) {
        this.strategies =
                strategies.stream()
                        .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                        .toList();
        this.conflictResolver = conflictResolver;
    }

    @Override
    public Collection<HandlerRegistration> discoverHandlers(Object source) {
        var allRegistrations = new ArrayList<HandlerRegistration>();
        var processedMessageTypes = new HashSet<Class<?>>();

        for (var strategy : strategies) {
            if (strategy.supports(source)) {
                try {
                    var registrations = strategy.discoverHandlers(source);
                    var filteredRegistrations =
                            resolveConflicts(registrations, processedMessageTypes);

                    allRegistrations.addAll(filteredRegistrations);
                    updateProcessedTypes(filteredRegistrations, processedMessageTypes);

                    // For FIRST_WINS and FAIL_FAST, stop at first successful strategy
                    if ((conflictResolver == ConflictResolutionStrategy.FIRST_WINS
                                    || conflictResolver == ConflictResolutionStrategy.FAIL_FAST)
                            && !filteredRegistrations.isEmpty()) {
                        break;
                    }

                } catch (RuntimeException e) {
                    log.warn(
                            "Strategy {} failed for source {}: {}",
                            strategy.getName(),
                            source.getClass().getSimpleName(),
                            e.getMessage());

                    if (conflictResolver == ConflictResolutionStrategy.FAIL_FAST) {
                        throw new HandlerDiscoveryException(
                                "Strategy execution failed: " + strategy.getName(),
                                strategy.getName(),
                                source,
                                e);
                    }
                }
            }
        }

        return allRegistrations;
    }

    @Override
    public boolean supports(Object source) {
        return strategies.stream().anyMatch(strategy -> strategy.supports(source));
    }

    @Override
    public String getName() {
        return "CompositeDiscovery";
    }

    /**
     * Resolves conflicts between registrations based on the configured strategy.
     *
     * @param registrations new registrations to process
     * @param processedMessageTypes message types already processed
     * @return filtered registrations after conflict resolution
     */
    private Collection<HandlerRegistration> resolveConflicts(
            Collection<HandlerRegistration> registrations,
            HashSet<Class<?>> processedMessageTypes) {

        var filteredRegistrations = new ArrayList<HandlerRegistration>();

        for (var registration : registrations) {
            var messageType = registration.messageType();

            switch (conflictResolver) {
                case FIRST_WINS -> {
                    if (!processedMessageTypes.contains(messageType)) {
                        filteredRegistrations.add(registration);
                    } else {
                        log.debug(
                                "Skipping duplicate registration for {} (FIRST_WINS)",
                                messageType.getSimpleName());
                    }
                }
                case LAST_WINS -> {
                    // Always add, later ones will overwrite
                    filteredRegistrations.add(registration);
                }
                case FAIL_FAST -> {
                    if (processedMessageTypes.contains(messageType)) {
                        throw new HandlerDiscoveryException(
                                "Duplicate handler registration for message type: "
                                        + messageType.getName(),
                                getName(),
                                registration.handlerInstance());
                    }
                    filteredRegistrations.add(registration);
                }
                case MERGE_ALL -> {
                    // Allow all registrations (useful for events)
                    filteredRegistrations.add(registration);
                }
            }
        }

        return filteredRegistrations;
    }

    /**
     * Updates the set of processed message types with newly filtered registrations.
     *
     * @param filteredRegistrations registrations that passed conflict resolution
     * @param processedMessageTypes set to update
     */
    private void updateProcessedTypes(
            Collection<HandlerRegistration> filteredRegistrations,
            HashSet<Class<?>> processedMessageTypes) {

        for (var registration : filteredRegistrations) {
            processedMessageTypes.add(registration.messageType());
        }
    }

    public static CompositeDiscoveryStrategy createDefault(
            ConflictResolutionStrategy conflictResolutionStrategy) {
        var strategies =
                List.of(
                        new AnnotationBasedDiscoveryStrategy(),
                        new InterfaceBasedDiscoveryStrategy());
        return new CompositeDiscoveryStrategy(strategies, conflictResolutionStrategy);
    }

    public static CompositeDiscoveryStrategy createDefault() {
        return createDefault(ConflictResolutionStrategy.FAIL_FAST);
    }
}
