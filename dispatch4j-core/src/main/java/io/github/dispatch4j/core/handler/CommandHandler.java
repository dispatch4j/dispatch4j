package io.github.dispatch4j.core.handler;

/**
 * Handler interface for processing commands in a CQRS architecture.
 *
 * <p>Commands are requests that modify system state and return a result. Each command type must
 * have exactly one registered handler. Multiple handlers for the same command type will result in a
 * {@link io.github.dispatch4j.core.exception.MultipleHandlersFoundException}.
 *
 * <p>This is a functional interface that can be implemented as a lambda expression or method
 * reference.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * CommandHandler<CreateUserCommand, User> handler = command -> {
 *     // Process command and return result
 *     return new User(command.name(), command.email());
 * };
 * }</pre>
 *
 * @param <C> the command type that this handler processes
 * @param <R> the return type of the handler
 * @see io.github.dispatch4j.core.annotation.Command
 * @see io.github.dispatch4j.core.annotation.CommandHandler
 */
@FunctionalInterface
public interface CommandHandler<C, R> extends RequestHandler<C, R> {

  /**
   * Processes the given command and returns a result.
   *
   * <p>This method should contain the business logic for handling the command. Any exceptions
   * thrown will be propagated to the caller.
   *
   * @param command the command to process (never null)
   * @return the result of processing the command
   * @throws RuntimeException if an error occurs during processing
   */
  @Override
  R handle(C command);
}
