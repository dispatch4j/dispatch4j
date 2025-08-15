package io.github.dispatch4j.handler;

/**
 * Handler interface for processing queries in a CQRS architecture.
 *
 * <p>Queries are requests that retrieve data without modifying system state. Each query type must
 * have exactly one registered handler. Multiple handlers for the same query type will result in a
 * {@link io.github.dispatch4j.exception.MultipleHandlersFoundException}.
 *
 * <p>This is a functional interface that can be implemented as a lambda expression or method
 * reference.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * QueryHandler<GetUserQuery, User> handler = query -> {
 *     // Retrieve and return data
 *     return userRepository.findById(query.userId());
 * };
 * }</pre>
 *
 * @param <Q> the query type that this handler processes
 * @param <R> the return type of the handler
 * @see io.github.dispatch4j.annotation.Query
 * @see io.github.dispatch4j.annotation.QueryHandler
 */
@FunctionalInterface
public interface QueryHandler<Q, R> extends RequestHandler<Q, R> {

    /**
     * Processes the given query and returns a result.
     *
     * <p>This method should contain the logic for retrieving the requested data. Any exceptions
     * thrown will be propagated to the caller.
     *
     * @param query the query to process (never null)
     * @return the result of processing the query
     * @throws RuntimeException if an error occurs during processing
     */
    @Override
    R handle(Q query);
}
