package io.github.dispatch4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method as a command handler in a CQRS architecture.
 *
 * <p>This annotation can be applied to:
 *
 * <ul>
 *   <li>Classes that implement {@link io.github.dispatch4j.handler.CommandHandler}
 *   <li>Methods that handle commands (must have exactly one parameter)
 * </ul>
 *
 * <p>When applied to methods, the method must:
 *
 * <ul>
 *   <li>Have exactly one parameter (the command)
 *   <li>Return a non-void value
 *   <li>Be in a Spring bean (for Spring Boot integration) or be discoverable by the standalone
 *       factory
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class UserCommandHandlers {
 *
 *     @CommandHandler
 *     public User handle(CreateUserCommand command) {
 *         // Handle command and return result
 *         return new User(command.name(), command.email());
 *     }
 * }
 * }</pre>
 *
 * @see io.github.dispatch4j.handler.CommandHandler
 * @see Command
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHandler {}
