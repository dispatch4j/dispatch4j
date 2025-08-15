package io.github.dispatch4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or record as a query in a CQRS architecture.
 *
 * <p>Queries are requests that retrieve data without modifying system state. Each query type must
 * have exactly one registered handler.
 *
 * <p>This annotation can only be applied to types (classes and records).
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Query
 * public record GetUserQuery(Long userId) {}
 * }</pre>
 *
 * @see io.github.dispatch4j.handler.QueryHandler
 * @see QueryHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {}
