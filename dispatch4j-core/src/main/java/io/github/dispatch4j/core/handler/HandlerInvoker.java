package io.github.dispatch4j.core.handler;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import io.github.dispatch4j.core.exception.Dispatch4jException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for safely invoking handler methods with proper exception handling. Provides
 * consistent behavior for method invocation across different frameworks.
 */
public final class HandlerInvoker {

  private HandlerInvoker() {}

  /**
   * Creates a CommandHandler that safely invokes the given method on the handler instance.
   *
   * @param method the method to invoke
   * @param handlerInstance the instance to invoke the method on
   * @return a CommandHandler that wraps the method invocation
   */
  public static CommandHandler<Object, Object> createCommandHandler(
      Method method, Object handlerInstance) {
    validateCommandHandlerMethod(method);
    method.trySetAccessible();
    return message -> invokeHandlerMethod(method, handlerInstance, message);
  }

  /**
   * Creates a QueryHandler that safely invokes the given method on the handler instance.
   *
   * @param method the method to invoke
   * @param handlerInstance the instance to invoke the method on
   * @return a QueryHandler that wraps the method invocation
   */
  public static QueryHandler<Object, Object> createQueryHandler(
      Method method, Object handlerInstance) {
    validateQueryHandlerMethod(method);
    method.trySetAccessible();
    return message -> invokeHandlerMethod(method, handlerInstance, message);
  }

  /**
   * Creates an EventHandler that safely invokes the given method on the handler instance.
   *
   * @param method the method to invoke
   * @param handlerInstance the instance to invoke the method on
   * @return an EventHandler that wraps the method invocation
   */
  public static EventHandler<Object> createEventHandler(Method method, Object handlerInstance) {
    validateEventHandlerMethod(method);
    method.trySetAccessible();
    return message -> invokeHandlerMethod(method, handlerInstance, message);
  }

  /**
   * Safely invokes a request handler method (Command/Query) with proper exception handling.
   * InvocationTargetExceptions are unwrapped, preserving RuntimeExceptions while wrapping checked
   * exceptions in Dispatch4jException.
   *
   * @param method the method to invoke
   * @param handlerInstance the instance to invoke the method on
   * @param message the message to pass to the handler
   * @return the result of the method invocation
   * @throws Dispatch4jException if the method invocation fails
   */
  private static Object invokeHandlerMethod(Method method, Object handlerInstance, Object message) {
    try {
      return method.invoke(handlerInstance, message);
    } catch (InvocationTargetException e) {
      var cause = e.getTargetException();
      if (cause instanceof RuntimeException re) {
        throw re;
      } else {
        throw new Dispatch4jException(
            "Failed to invoke handler method: %s".formatted(method.getName()), cause);
      }
    } catch (Exception e) {
      throw new Dispatch4jException(
          "Failed to invoke handler method: %s".formatted(method.getName()), e);
    }
  }

  private static void validateCommandHandlerMethod(Method method) {
    if (method.getParameterCount() != 1) {
      throw new Dispatch4jException(
          "Command handler method %s must have exactly one parameter".formatted(method.getName()));
    }

    var parameterType = method.getParameterTypes()[0];
    if (!parameterType.isAnnotationPresent(Command.class)) {
      throw new Dispatch4jException(
          "Command handler method %s parameter type must be annotated with @Command"
              .formatted(method.getName()));
    }

    if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
      throw new Dispatch4jException(
          "Command handler method %s must return a value".formatted(method.getName()));
    }
  }

  private static void validateQueryHandlerMethod(Method method) {
    if (method.getParameterCount() != 1) {
      throw new Dispatch4jException(
          "Query handler method %s must have exactly one parameter".formatted(method.getName()));
    }

    var parameterType = method.getParameterTypes()[0];
    if (!parameterType.isAnnotationPresent(Query.class)) {
      throw new Dispatch4jException(
          "Query handler method %s parameter type must be annotated with @Query"
              .formatted(method.getName()));
    }

    if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
      throw new Dispatch4jException(
          "Query handler method %s must return a value".formatted(method.getName()));
    }
  }

  private static void validateEventHandlerMethod(Method method) {
    if (method.getParameterCount() != 1) {
      throw new Dispatch4jException(
          "Event handler method %s must have exactly one parameter".formatted(method.getName()));
    }

    var parameterType = method.getParameterTypes()[0];
    if (!parameterType.isAnnotationPresent(Event.class)) {
      throw new Dispatch4jException(
          "Event handler method %s parameter type must be annotated with @Event"
              .formatted(method.getName()));
    }

    if (method.getReturnType() != void.class && method.getReturnType() != Void.class) {
      throw new Dispatch4jException(
          "Event handler method %s must return void".formatted(method.getName()));
    }
  }
}
