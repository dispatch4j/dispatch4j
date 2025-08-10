package io.github.dispatch4j.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or record as a command in a CQRS architecture.
 *
 * <p>Commands are requests that modify system state and return a result. Each command type must
 * have exactly one registered handler.
 *
 * <p>This annotation can be applied to:
 *
 * <ul>
 *   <li>Classes (including record classes)
 *   <li>Record components
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Command
 * public record CreateUserCommand(String name, String email) {}
 * }</pre>
 *
 * @see io.github.dispatch4j.core.handler.CommandHandler
 * @see CommandHandler
 */
@Target({ElementType.TYPE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {}
