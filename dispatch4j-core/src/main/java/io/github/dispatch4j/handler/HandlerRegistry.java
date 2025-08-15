package io.github.dispatch4j.handler;

import io.github.dispatch4j.Dispatch4j;
import io.github.dispatch4j.discovery.AnnotationBasedDiscoveryStrategy;
import io.github.dispatch4j.discovery.CompositeDiscoveryStrategy;
import io.github.dispatch4j.discovery.HandlerDiscoveryStrategy;
import io.github.dispatch4j.discovery.InterfaceBasedDiscoveryStrategy;
import io.github.dispatch4j.exception.Dispatch4jException;
import io.github.dispatch4j.exception.MultipleHandlersFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe registry for managing command, query, and event handlers.
 *
 * <p>This class maintains mappings between message types and their corresponding handlers using
 * {@link ConcurrentHashMap} for thread-safe operation. It enforces CQRS constraints:
 *
 * <ul>
 *   <li>Commands and queries can have exactly one handler
 *   <li>Events can have zero to many handlers
 * </ul>
 *
 * <p>Registration constraints:
 *
 * <ul>
 *   <li>Attempting to register multiple handlers for the same command/query type will throw {@link
 *       MultipleHandlersFoundException}
 *   <li>Null handlers or message types will throw {@link Dispatch4jException}
 * </ul>
 *
 * <p>This class is thread-safe and can be used concurrently by multiple threads.
 *
 * @see HandlerRegistrar
 * @see Dispatch4j
 */
public class HandlerRegistry implements HandlerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(HandlerRegistry.class);

    private final Map<Class<?>, CommandHandler<?, ?>> commandHandlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, QueryHandler<?, ?>> queryHandlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<EventHandler<?>>> eventHandlers = new ConcurrentHashMap<>();
    private final HandlerDiscoveryStrategy discoveryStrategy;

    /** Creates a new HandlerRegistry with default discovery strategy. */
    public HandlerRegistry() {
        this(createDefaultStrategy());
    }

    /**
     * Creates a new HandlerRegistry with the specified discovery strategy.
     *
     * @param discoveryStrategy the discovery strategy to use
     */
    public HandlerRegistry(HandlerDiscoveryStrategy discoveryStrategy) {
        this.discoveryStrategy = discoveryStrategy;
    }

    /**
     * Retrieves the command handler for the specified command type.
     *
     * @param <R> the return type of the handler
     * @param commandType the command type to find a handler for (must not be null)
     * @return the command handler, or null if no handler is registered
     * @throws Dispatch4jException if commandType is null
     */
    @SuppressWarnings("unchecked")
    public <R> CommandHandler<Object, R> getCommandHandler(Class<?> commandType) {
        if (commandType == null) {
            throw new Dispatch4jException("Command type cannot be null");
        }
        return (CommandHandler<Object, R>) commandHandlers.get(commandType);
    }

    /**
     * Retrieves the query handler for the specified query type.
     *
     * @param <R> the return type of the handler
     * @param queryType the query type to find a handler for (must not be null)
     * @return the query handler, or null if no handler is registered
     * @throws Dispatch4jException if queryType is null
     */
    @SuppressWarnings("unchecked")
    public <R> QueryHandler<Object, R> getQueryHandler(Class<?> queryType) {
        if (queryType == null) {
            throw new Dispatch4jException("Query type cannot be null");
        }
        return (QueryHandler<Object, R>) queryHandlers.get(queryType);
    }

    /**
     * Retrieves all event handlers for the specified event type.
     *
     * @param eventType the event type to find handlers for (must not be null)
     * @return an immutable list of event handlers, empty if no handlers are registered
     * @throws Dispatch4jException if eventType is null
     */
    public List<EventHandler<?>> getEventHandlers(Class<?> eventType) {
        if (eventType == null) {
            throw new Dispatch4jException("Event type cannot be null");
        }
        return eventHandlers.getOrDefault(eventType, List.of());
    }

    /**
     * Registers a command handler for the specified command type.
     *
     * <p>Command types can only have one handler. Attempting to register a second handler for the
     * same command type will throw {@link MultipleHandlersFoundException}.
     *
     * @param commandType the command type to register the handler for (must not be null)
     * @param handler the command handler to register (must not be null)
     * @throws Dispatch4jException if handler or commandType is null
     * @throws MultipleHandlersFoundException if a handler is already registered for this command
     *     type
     */
    @Override
    public <T> void registerCommandHandler(Class<T> commandType, CommandHandler<T, ?> handler) {
        if (handler == null) {
            throw new Dispatch4jException("Command handler cannot be null");
        }
        if (commandType == null) {
            throw new Dispatch4jException("Command type cannot be null");
        }
        if (commandHandlers.containsKey(commandType)) {
            throw new MultipleHandlersFoundException(commandType, 2);
        }
        commandHandlers.put(commandType, handler);
    }

    /**
     * Registers a query handler for the specified query type.
     *
     * <p>Query types can only have one handler. Attempting to register a second handler for the
     * same query type will throw {@link MultipleHandlersFoundException}.
     *
     * @param queryType the query type to register the handler for (must not be null)
     * @param handler the query handler to register (must not be null)
     * @throws Dispatch4jException if handler or queryType is null
     * @throws MultipleHandlersFoundException if a handler is already registered for this query type
     */
    @Override
    public <T> void registerQueryHandler(Class<T> queryType, QueryHandler<T, ?> handler) {
        if (handler == null) {
            throw new Dispatch4jException("Query handler cannot be null");
        }
        if (queryType == null) {
            throw new Dispatch4jException("Query type cannot be null");
        }
        if (queryHandlers.containsKey(queryType)) {
            throw new MultipleHandlersFoundException(queryType, 2);
        }
        queryHandlers.put(queryType, handler);
    }

    /**
     * Registers an event handler for the specified event type.
     *
     * <p>Event types can have multiple handlers. This method will add the handler to the list of
     * handlers for the event type.
     *
     * @param eventType the event type to register the handler for (must not be null)
     * @param handler the event handler to register (must not be null)
     * @throws Dispatch4jException if handler or eventType is null
     */
    @Override
    public <T> void registerEventHandler(Class<T> eventType, EventHandler<T> handler) {
        if (handler == null) {
            throw new Dispatch4jException("Event handler cannot be null");
        }
        if (eventType == null) {
            throw new Dispatch4jException("Event type cannot be null");
        }
        eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * THE SINGLE ENTRY POINT for handler discovery and registration. This is the HandlerRegistrar
     * interface method that MUST be implemented. ALL discovery operations should flow through this
     * method.
     */
    @Override
    public void registerHandlersIn(Object handler) {
        if (handler == null) {
            throw new Dispatch4jException("Handler cannot be null");
        }

        // 1. Use strategy to discover handlers (returns metadata only)
        var discoveredHandlers = discoveryStrategy.discoverHandlers(handler);

        // 2. Register each discovered handler using existing registration methods
        for (var registration : discoveredHandlers) {
            registerDiscoveredHandler(registration);
        }

        log.debug(
                "Registered {} handlers from object: {}",
                discoveredHandlers.size(),
                handler.getClass().getSimpleName());
    }

    /** Internal method to register handlers discovered by strategies. */
    private void registerDiscoveredHandler(
            io.github.dispatch4j.discovery.HandlerRegistration registration) {
        var handler = registration.handlerInstance();
        var messageType = registration.messageType();

        switch (registration.handlerKind()) {
            case COMMAND -> {
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                CommandHandler<Object, ?> commandHandler = (CommandHandler<Object, ?>) handler;
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                Class<Object> typedMessageType = (Class<Object>) messageType;
                registerCommandHandler(typedMessageType, commandHandler);
            }
            case QUERY -> {
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                QueryHandler<Object, ?> queryHandler = (QueryHandler<Object, ?>) handler;
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                Class<Object> typedMessageType = (Class<Object>) messageType;
                registerQueryHandler(typedMessageType, queryHandler);
            }
            case EVENT -> {
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                EventHandler<Object> eventHandler = (EventHandler<Object>) handler;
                @SuppressWarnings("unchecked") // Safe cast - validated by strategy
                Class<Object> typedMessageType = (Class<Object>) messageType;
                registerEventHandler(typedMessageType, eventHandler);
            }
        }
    }

    /**
     * Creates the default discovery strategy with both annotation and interface-based discovery.
     */
    private static HandlerDiscoveryStrategy createDefaultStrategy() {
        return new CompositeDiscoveryStrategy(
                List.of(
                        new AnnotationBasedDiscoveryStrategy(),
                        new InterfaceBasedDiscoveryStrategy()));
    }
}
