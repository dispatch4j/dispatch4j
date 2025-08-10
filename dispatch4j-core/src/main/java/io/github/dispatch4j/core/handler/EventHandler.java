package io.github.dispatch4j.core.handler;

/**
 * Handler interface for processing events in a CQRS architecture.
 *
 * <p>Events are notifications that something has happened in the system. They are fire-and-forget
 * operations that do not return a value. Unlike commands and queries, events can have zero, one, or
 * multiple handlers.
 *
 * <p>This is a functional interface that can be implemented as a lambda expression or method
 * reference.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EventHandler<UserCreatedEvent> handler = event -> {
 *     // Handle the event (e.g., send welcome email)
 *     emailService.sendWelcomeEmail(event.userEmail());
 * };
 * }</pre>
 *
 * @param <E> the event type that this handler processes
 * @see io.github.dispatch4j.core.annotation.Event
 * @see io.github.dispatch4j.core.annotation.EventHandler
 */
@FunctionalInterface
public interface EventHandler<E> {

  /**
   * Processes the given event.
   *
   * <p>This method should contain the logic for handling the event. Any exceptions thrown will be
   * propagated to the caller and may prevent other event handlers from being executed.
   *
   * @param event the event to process (never null)
   * @throws RuntimeException if an error occurs during processing
   */
  void handle(E event);
}
