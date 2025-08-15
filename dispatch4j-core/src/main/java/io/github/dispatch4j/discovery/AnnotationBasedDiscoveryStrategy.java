package io.github.dispatch4j.discovery;

import io.github.dispatch4j.exception.HandlerDiscoveryException;
import io.github.dispatch4j.exception.HandlerValidationException;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.HandlerInvoker;
import io.github.dispatch4j.handler.QueryHandler;
import io.github.dispatch4j.util.AnnotationFinder;
import io.github.dispatch4j.util.CoreAnnotationFinder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery strategy that finds handlers via method annotations.
 *
 * <p>This strategy is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/strategy/AnnotationBasedDiscoveryStrategy.java.
 * It scans methods for @CommandHandler, @QueryHandler, and @EventHandler annotations using a
 * pluggable AnnotationUtils abstraction. This allows the same strategy to work in both core (using
 * standard reflection) and Spring (using enhanced annotation detection) environments.
 *
 * <p>The strategy validates method signatures and creates HandlerInvoker wrappers for discovered
 * handlers.
 */
public class AnnotationBasedDiscoveryStrategy implements HandlerDiscoveryStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(AnnotationBasedDiscoveryStrategy.class);

    private final AnnotationFinder annotationUtils;

    /** Creates a new AnnotationBasedDiscoveryStrategy with default core annotation utils. */
    public AnnotationBasedDiscoveryStrategy() {
        this(new CoreAnnotationFinder());
    }

    /**
     * Creates a new AnnotationBasedDiscoveryStrategy with the specified annotation utils.
     *
     * @param annotationUtils the annotation detection utilities to use
     */
    public AnnotationBasedDiscoveryStrategy(AnnotationFinder annotationUtils) {
        this.annotationUtils = annotationUtils;
    }

    @Override
    public Collection<HandlerRegistration> discoverHandlers(Object source) {
        if (!supports(source)) {
            return List.of();
        }

        var registrations = new ArrayList<HandlerRegistration>();
        var sourceClass = source.getClass();

        for (Method method : sourceClass.getMethods()) {
            try {
                if (annotationUtils.isCommandHandler(method)) {
                    var registration = processCommandHandler(source, method);
                    registrations.add(registration);
                }

                if (annotationUtils.isQueryHandler(method)) {
                    var registration = processQueryHandler(source, method);
                    registrations.add(registration);
                }

                if (annotationUtils.isEventHandler(method)) {
                    var registration = processEventHandler(source, method);
                    registrations.add(registration);
                }
            } catch (RuntimeException e) {
                throw new HandlerDiscoveryException(
                        "Failed to process handler method: " + method.getName(),
                        getName(),
                        source,
                        e);
            }
        }

        log.debug(
                "Discovered {} annotation-based handlers from {} using {}",
                registrations.size(),
                sourceClass.getSimpleName(),
                annotationUtils.getDetectionStrategyName());

        return registrations;
    }

    @Override
    public boolean supports(Object source) {
        if (source == null) {
            return false;
        }

        // Check if any methods have handler annotations
        for (Method method : source.getClass().getMethods()) {
            if (annotationUtils.isAnyHandler(method)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getPriority() {
        return 100; // Higher priority than interface-based
    }

    @Override
    public String getName() {
        return "AnnotationBasedDiscovery(" + annotationUtils.getDetectionStrategyName() + ")";
    }

    private HandlerRegistration processCommandHandler(Object source, Method method) {
        try {
            validateCommandHandlerMethod(method);

            var handler = HandlerInvoker.createCommandHandler(method, source);
            var messageType = method.getParameterTypes()[0];

            @SuppressWarnings("unchecked") // Safe cast - validated by createCommandHandler
            var typedHandler = (CommandHandler<Object, ?>) handler;
            @SuppressWarnings("unchecked") // Safe cast - we know this is the message type
            var typedMessageType = (Class<Object>) messageType;

            return HandlerRegistration.commandHandler(
                    typedMessageType, source.getClass(), typedHandler, method.getName(), getName());
        } catch (IllegalArgumentException e) {
            throw new HandlerValidationException(
                    "Invalid command handler method signature: " + method.getName(),
                    getName(),
                    source,
                    "Method signature validation",
                    e);
        } catch (RuntimeException e) {
            throw new HandlerDiscoveryException(
                    "Failed to process command handler: " + method.getName(), getName(), source, e);
        }
    }

    private HandlerRegistration processQueryHandler(Object source, Method method) {
        try {
            validateQueryHandlerMethod(method);

            var handler = HandlerInvoker.createQueryHandler(method, source);
            var messageType = method.getParameterTypes()[0];

            @SuppressWarnings("unchecked") // Safe cast - validated by createQueryHandler
            var typedHandler = (QueryHandler<Object, ?>) handler;
            @SuppressWarnings("unchecked") // Safe cast - we know this is the message type
            var typedMessageType = (Class<Object>) messageType;

            return HandlerRegistration.queryHandler(
                    typedMessageType, source.getClass(), typedHandler, method.getName(), getName());
        } catch (IllegalArgumentException e) {
            throw new HandlerValidationException(
                    "Invalid query handler method signature: " + method.getName(),
                    getName(),
                    source,
                    "Method signature validation",
                    e);
        } catch (RuntimeException e) {
            throw new HandlerDiscoveryException(
                    "Failed to process query handler: " + method.getName(), getName(), source, e);
        }
    }

    private HandlerRegistration processEventHandler(Object source, Method method) {
        try {
            validateEventHandlerMethod(method);

            var handler = HandlerInvoker.createEventHandler(method, source);
            var messageType = method.getParameterTypes()[0];

            @SuppressWarnings("unchecked") // Safe cast - we know this is the message type
            var typedMessageType = (Class<Object>) messageType;

            return HandlerRegistration.eventHandler(
                    typedMessageType, source.getClass(), handler, method.getName(), getName());
        } catch (IllegalArgumentException e) {
            throw new HandlerValidationException(
                    "Invalid event handler method signature: " + method.getName(),
                    getName(),
                    source,
                    "Method signature validation",
                    e);
        } catch (RuntimeException e) {
            throw new HandlerDiscoveryException(
                    "Failed to process event handler: " + method.getName(), getName(), source, e);
        }
    }

    private void validateCommandHandlerMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException(
                    "Command handler method must have exactly one parameter");
        }
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException(
                    "Command handler method must return a value (non-void)");
        }
    }

    private void validateQueryHandlerMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException(
                    "Query handler method must have exactly one parameter");
        }
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException(
                    "Query handler method must return a value (non-void)");
        }
    }

    private void validateEventHandlerMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException(
                    "Event handler method must have exactly one parameter");
        }
        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException("Event handler method must return void");
        }
    }
}
