package io.github.dispatch4j.core;

import io.github.dispatch4j.core.exception.HandlerNotFoundException;
import io.github.dispatch4j.core.handler.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The core implementation of the CQRS dispatcher that handles commands, queries, and events.
 *
 * <p>This class is the main entry point for the Dispatch4j library, providing both synchronous and
 * asynchronous methods for sending commands/queries and publishing events. It maintains a registry
 * of handlers and routes messages to their appropriate handlers.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Thread-safe operation using {@link java.util.concurrent.ConcurrentHashMap} internally
 *   <li>Support for both synchronous and asynchronous operations
 *   <li>Automatic handler resolution based on message types
 *   <li>Configurable executor for async operations
 * </ul>
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li>Commands: Must have exactly one handler, return values
 *   <li>Queries: Must have exactly one handler, return values
 *   <li>Events: Can have zero to many handlers, return void
 * </ul>
 *
 * <p>This class is final and thread-safe.
 *
 * @see Dispatcher
 * @see HandlerRegistrar
 */
public final class Dispatch4j implements Dispatcher, HandlerRegistrar {

  private static final Logger log = LoggerFactory.getLogger(Dispatch4j.class);

  private final HandlerRegistry handlerRegistry;
  private final Executor executor;

  /**
   * Creates a new Dispatch4j instance with default configuration.
   *
   * <p>Uses a new {@link HandlerRegistry} and the {@link ForkJoinPool#commonPool()} for
   * asynchronous operations.
   */
  public Dispatch4j() {
    this(new HandlerRegistry(), ForkJoinPool.commonPool());
  }

  /**
   * Creates a new Dispatch4j instance with a custom executor.
   *
   * <p>Uses a new {@link HandlerRegistry} and the provided executor for asynchronous operations.
   *
   * @param executor the executor to use for async operations (must not be null)
   */
  public Dispatch4j(Executor executor) {
    this(new HandlerRegistry(), executor);
  }

  /**
   * Creates a new Dispatch4j instance with custom handler registry and executor.
   *
   * <p>This constructor allows full customization of both the handler registry and the executor
   * used for asynchronous operations.
   *
   * @param handlerRegistry the handler registry to use (must not be null)
   * @param executor the executor to use for async operations (must not be null)
   */
  public Dispatch4j(HandlerRegistry handlerRegistry, Executor executor) {
    this.handlerRegistry = handlerRegistry;
    this.executor = executor;
  }

  @Override
  public <R> R send(Object message) {
    var messageType = message.getClass();

    // Try command handlers first
    CommandHandler<Object, R> commandHandler = handlerRegistry.getCommandHandler(messageType);
    if (commandHandler != null) {
      log.debug("Sending command: {}", messageType.getSimpleName());
      return commandHandler.handle(message);
    }

    // Try query handlers
    QueryHandler<Object, R> queryHandler = handlerRegistry.getQueryHandler(messageType);
    if (queryHandler != null) {
      log.debug("Sending query: {}", messageType.getSimpleName());
      return queryHandler.handle(message);
    }

    throw new HandlerNotFoundException(messageType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void publish(Object event) {
    var eventType = event.getClass();
    var handlers = handlerRegistry.getEventHandlers(eventType);

    log.debug("Publishing event: {} to {} handlers", eventType.getSimpleName(), handlers.size());
    if (handlers.isEmpty()) {
      log.warn("No handlers found for event: {}", eventType.getSimpleName());
      return;
    }
    for (EventHandler<?> handler : handlers) {
      ((EventHandler<Object>) handler).handle(event);
    }
  }

  @Override
  public <R> CompletableFuture<R> sendAsync(Object message) {
    return CompletableFuture.supplyAsync(() -> send(message), executor);
  }

  @Override
  public CompletableFuture<Void> publishAsync(Object event) {
    return CompletableFuture.runAsync(() -> publish(event), executor);
  }

  @Override
  public void registerCommandHandler(Class<?> commandType, CommandHandler<?, ?> handler) {
    handlerRegistry.registerCommandHandler(commandType, handler);
  }

  @Override
  public void registerQueryHandler(Class<?> queryType, QueryHandler<?, ?> handler) {
    handlerRegistry.registerQueryHandler(queryType, handler);
  }

  @Override
  public void registerEventHandler(Class<?> eventType, EventHandler<?> handler) {
    handlerRegistry.registerEventHandler(eventType, handler);
  }
}
