package io.github.dispatch4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method as a query handler in a CQRS architecture.
 *
 * <p>This annotation can be applied to:
 *
 * <ul>
 *   <li>Classes that implement {@link io.github.dispatch4j.handler.QueryHandler}
 *   <li>Methods that handle queries (must have exactly one parameter)
 * </ul>
 *
 * <p>When applied to methods, the method must:
 *
 * <ul>
 *   <li>Have exactly one parameter (the query)
 *   <li>Return a non-void value
 *   <li>Be in a Spring bean (for Spring Boot integration) or be discoverable by the standalone
 *       factory
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class UserQueryHandlers {
 *
 *     @QueryHandler
 *     public User handle(GetUserQuery query) {
 *         // Handle query and return result
 *         return userRepository.findById(query.userId());
 *     }
 * }
 * }</pre>
 *
 * @see io.github.dispatch4j.handler.QueryHandler
 * @see Query
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryHandler {}
