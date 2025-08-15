package io.github.dispatch4j.discovery;

import io.github.dispatch4j.exception.HandlerDiscoveryException;
import io.github.dispatch4j.exception.HandlerValidationException;
import io.github.dispatch4j.exception.TypeResolutionException;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.EventHandler;
import io.github.dispatch4j.handler.QueryHandler;
import io.github.dispatch4j.util.GenericTypeResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery strategy that finds handlers via interface implementation.
 *
 * <p>This strategy is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/strategy/InterfaceBasedDiscoveryStrategy.java.
 * It scans for classes implementing CommandHandler&lt;T,R&gt;, QueryHandler&lt;T,R&gt;, and
 * EventHandler&lt;T&gt; interfaces. Uses generic type resolution to determine message types and
 * validates message type annotations.
 */
public class InterfaceBasedDiscoveryStrategy implements HandlerDiscoveryStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(InterfaceBasedDiscoveryStrategy.class);
    private final GenericTypeResolver typeResolver = new GenericTypeResolver();

    @Override
    public Collection<HandlerRegistration> discoverHandlers(Object source) {
        if (!supports(source)) {
            return List.of();
        }

        var registrations = new ArrayList<HandlerRegistration>();
        var sourceClass = source.getClass();

        // Check for CommandHandler interface
        if (source instanceof CommandHandler<?, ?> commandHandler) {
            try {
                var registration = processCommandHandler(commandHandler, sourceClass);
                registrations.add(registration);
            } catch (TypeResolutionException e) {
                log.debug(
                        "Skipping CommandHandler {} due to type resolution failure: {}",
                        sourceClass.getSimpleName(),
                        e.getMessage());
                // Skip handlers that can't resolve their message types (e.g., raw types)
            }
        }

        // Check for QueryHandler interface
        if (source instanceof QueryHandler<?, ?> queryHandler) {
            try {
                var registration = processQueryHandler(queryHandler, sourceClass);
                registrations.add(registration);
            } catch (TypeResolutionException e) {
                log.debug(
                        "Skipping QueryHandler {} due to type resolution failure: {}",
                        sourceClass.getSimpleName(),
                        e.getMessage());
                // Skip handlers that can't resolve their message types (e.g., raw types)
            }
        }

        // Check for EventHandler interface
        if (source instanceof EventHandler<?> eventHandler) {
            try {
                var registration = processEventHandler(eventHandler, sourceClass);
                registrations.add(registration);
            } catch (TypeResolutionException e) {
                log.debug(
                        "Skipping EventHandler {} due to type resolution failure: {}",
                        sourceClass.getSimpleName(),
                        e.getMessage());
                // Skip handlers that can't resolve their message types (e.g., raw types)
            }
        }

        log.debug(
                "Discovered {} interface-based handlers from {}",
                registrations.size(),
                sourceClass.getSimpleName());

        return registrations;
    }

    @Override
    public boolean supports(Object source) {
        return source instanceof CommandHandler<?, ?>
                || source instanceof QueryHandler<?, ?>
                || source instanceof EventHandler<?>;
    }

    @Override
    public int getPriority() {
        return 50; // Lower priority than annotation-based
    }

    @Override
    public String getName() {
        return "InterfaceBasedDiscovery";
    }

    /** Generic handler processing method that eliminates duplication between handler types. */
    private <T> HandlerRegistration processHandler(
            T handler,
            Class<?> handlerClass,
            TypeResolver typeResolver,
            RegistrationFactory<T> registrationFactory) {
        try {
            var messageType = typeResolver.resolve(handlerClass);
            validateMessageType(messageType);

            @SuppressWarnings("unchecked") // Safe cast - we control the creation
            var typedMessageType = (Class<Object>) messageType;

            return registrationFactory.create(
                    typedMessageType,
                    handlerClass,
                    handler,
                    handlerClass.getSimpleName(),
                    getName());
        } catch (TypeResolutionException e) {
            throw e; // Re-throw TypeResolutionException as-is
        } catch (RuntimeException e) {
            throw new HandlerDiscoveryException(
                    "Failed to process interface-based handler: " + handlerClass.getSimpleName(),
                    getName(),
                    handler,
                    e);
        }
    }

    private HandlerRegistration processCommandHandler(
            CommandHandler<?, ?> handler, Class<?> handlerClass) {
        return processHandler(
                handler,
                handlerClass,
                typeResolver::resolveCommandType,
                (messageType, handlerType, typedHandler, name, source) -> {
                    @SuppressWarnings("unchecked") // Safe cast - source is CommandHandler
                    var commandHandler = (CommandHandler<Object, ?>) typedHandler;
                    return HandlerRegistration.commandHandler(
                            messageType, handlerType, commandHandler, name, source);
                });
    }

    private HandlerRegistration processQueryHandler(
            QueryHandler<?, ?> handler, Class<?> handlerClass) {
        return processHandler(
                handler,
                handlerClass,
                typeResolver::resolveQueryType,
                (messageType, handlerType, typedHandler, name, source) -> {
                    @SuppressWarnings("unchecked") // Safe cast - source is QueryHandler
                    var queryHandler = (QueryHandler<Object, ?>) typedHandler;
                    return HandlerRegistration.queryHandler(
                            messageType, handlerType, queryHandler, name, source);
                });
    }

    private HandlerRegistration processEventHandler(
            EventHandler<?> handler, Class<?> handlerClass) {
        return processHandler(
                handler,
                handlerClass,
                typeResolver::resolveEventType,
                (messageType, handlerType, typedHandler, name, source) -> {
                    @SuppressWarnings("unchecked") // Safe cast - source is EventHandler
                    var eventHandler = (EventHandler<Object>) typedHandler;
                    return HandlerRegistration.eventHandler(
                            messageType, handlerType, eventHandler, name, source);
                });
    }

    @FunctionalInterface
    private interface TypeResolver {
        Class<?> resolve(Class<?> handlerClass);
    }

    @FunctionalInterface
    private interface RegistrationFactory<T> {
        HandlerRegistration create(
                Class<Object> messageType,
                Class<?> handlerType,
                T handler,
                String name,
                String source);
    }

    private void validateMessageType(Class<?> messageType) {
        if (messageType == null) {
            throw new TypeResolutionException(
                    "Could not resolve message type from handler interface",
                    null); // Will be set by caller context
        }

        // Additional validation for message type annotations
        var hasCommandAnnotation =
                messageType.isAnnotationPresent(io.github.dispatch4j.annotation.Command.class);
        var hasQueryAnnotation =
                messageType.isAnnotationPresent(io.github.dispatch4j.annotation.Query.class);
        var hasEventAnnotation =
                messageType.isAnnotationPresent(io.github.dispatch4j.annotation.Event.class);

        var annotationCount =
                (hasCommandAnnotation ? 1 : 0)
                        + (hasQueryAnnotation ? 1 : 0)
                        + (hasEventAnnotation ? 1 : 0);

        if (annotationCount == 0) {
            throw new HandlerValidationException(
                    "Message type must be annotated with @Command, @Query, or @Event: "
                            + messageType.getName(),
                    getName(),
                    messageType,
                    "Missing message type annotation");
        }

        if (annotationCount > 1) {
            throw new HandlerValidationException(
                    "Message type cannot have multiple message annotations: "
                            + messageType.getName(),
                    getName(),
                    messageType,
                    "Multiple message type annotations");
        }
    }
}
