package io.github.dispatch4j.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method as an event handler in a CQRS architecture.
 *
 * <p>This annotation can be applied to:
 *
 * <ul>
 *   <li>Classes that implement {@link io.github.dispatch4j.core.handler.EventHandler}
 *   <li>Methods that handle events (must have exactly one parameter)
 * </ul>
 *
 * <p>When applied to methods, the method must:
 *
 * <ul>
 *   <li>Have exactly one parameter (the event)
 *   <li>Return void
 *   <li>Be in a Spring bean (for Spring Boot integration) or be discoverable by the standalone
 *       factory
 * </ul>
 *
 * <p>Unlike command and query handlers, multiple event handlers can be registered for the same
 * event type.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class UserEventHandlers {
 *
 *     @EventHandler
 *     public void handle(UserCreatedEvent event) {
 *         // Handle event (e.g., send welcome email)
 *         emailService.sendWelcomeEmail(event.userEmail());
 *     }
 * }
 * }</pre>
 *
 * @see io.github.dispatch4j.core.handler.EventHandler
 * @see Event
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {}
