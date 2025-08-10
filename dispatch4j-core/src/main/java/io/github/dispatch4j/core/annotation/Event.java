package io.github.dispatch4j.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or record as an event in a CQRS architecture.
 *
 * <p>Events are notifications that something has happened in the system. They are fire-and-forget
 * operations that do not return a value. Unlike commands and queries, events can have zero, one, or
 * multiple handlers.
 *
 * <p>This annotation can only be applied to types (classes and records).
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Event
 * public record UserCreatedEvent(Long userId, String name, String email) {}
 * }</pre>
 *
 * @see io.github.dispatch4j.core.handler.EventHandler
 * @see EventHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Event {}
