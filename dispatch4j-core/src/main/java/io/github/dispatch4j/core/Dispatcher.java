package io.github.dispatch4j.core;

import java.util.concurrent.CompletableFuture;

/**
 * The main dispatcher interface for sending commands/queries and publishing events in a CQRS
 * (Command Query Responsibility Segregation) architecture.
 *
 * <p>This interface provides both synchronous and asynchronous methods for:
 *
 * <ul>
 *   <li>Sending commands and queries that return values
 *   <li>Publishing events that do not return values
 * </ul>
 *
 * <p>All implementations must be thread-safe as dispatchers are typically used concurrently across
 * multiple threads in an application.
 *
 * @see Dispatch4j
 */
public interface Dispatcher {

    /**
     * Synchronously sends a command or query to its registered handler.
     *
     * <p>Commands and queries must have exactly one registered handler. If no handler is found, a
     * {@link io.github.dispatch4j.core.exception.HandlerNotFoundException} is thrown.
     *
     * @param <R> the return type of the handler
     * @param command the command or query to send (must not be null)
     * @return the result returned by the handler
     * @throws io.github.dispatch4j.core.exception.HandlerNotFoundException if no handler is
     *     registered for the message type
     * @throws RuntimeException if the handler throws an exception during processing
     */
    <R> R send(Object command);

    /**
     * Synchronously publishes an event to all its registered handlers.
     *
     * <p>Events can have zero, one, or multiple handlers. If no handlers are registered, a warning
     * is logged but no exception is thrown.
     *
     * @param event the event to publish (must not be null)
     * @throws RuntimeException if any handler throws an exception during processing
     */
    void publish(Object event);

    /**
     * Asynchronously sends a command or query to its registered handler.
     *
     * <p>This method returns immediately with a {@link CompletableFuture} that will complete when
     * the handler finishes processing the message.
     *
     * @param <R> the return type of the handler
     * @param command the command or query to send (must not be null)
     * @return a CompletableFuture that will complete with the handler result
     * @see #send(Object)
     */
    <R> CompletableFuture<R> sendAsync(Object command);

    /**
     * Asynchronously publishes an event to all its registered handlers.
     *
     * <p>This method returns immediately with a {@link CompletableFuture} that will complete when
     * all handlers have finished processing the event.
     *
     * @param event the event to publish (must not be null)
     * @return a CompletableFuture that will complete when all handlers finish
     * @see #publish(Object)
     */
    CompletableFuture<Void> publishAsync(Object event);
}
