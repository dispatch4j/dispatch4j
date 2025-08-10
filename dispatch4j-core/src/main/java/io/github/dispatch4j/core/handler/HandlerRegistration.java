package io.github.dispatch4j.core.handler;

import io.github.dispatch4j.core.annotation.Command;
import io.github.dispatch4j.core.annotation.Event;
import io.github.dispatch4j.core.annotation.Query;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for registering handler classes that implement handler interfaces. Supports both
 * CommandHandler, QueryHandler, and EventHandler interfaces.
 */
final class HandlerRegistration {

  private static final Logger log = LoggerFactory.getLogger(HandlerRegistration.class);

  private HandlerRegistration() {}

  /**
   * Registers a handler instance that implements one of the handler interfaces. Automatically
   * detects the implemented interfaces and registers appropriately.
   *
   * @param handlerInstance the handler instance to register
   * @param commandRegistrar function to register command handlers
   * @param queryRegistrar function to register query handlers
   * @param eventRegistrar function to register event handlers
   */
  public static void registerInterfaceBasedHandler(
      Object handlerInstance,
      BiConsumer<Class<?>, CommandHandler<?, ?>> commandRegistrar,
      BiConsumer<Class<?>, QueryHandler<?, ?>> queryRegistrar,
      BiConsumer<Class<?>, EventHandler<?>> eventRegistrar) {
    var handlerClass = handlerInstance.getClass();
    var interfaces = handlerClass.getGenericInterfaces();

    for (Type interfaceType : interfaces) {
      if (interfaceType instanceof ParameterizedType parameterizedType) {
        var rawType = parameterizedType.getRawType();

        if (rawType == CommandHandler.class) {
          registerCommandHandlerInterface(handlerInstance, parameterizedType, commandRegistrar);
        } else if (rawType == QueryHandler.class) {
          registerQueryHandlerInterface(handlerInstance, parameterizedType, queryRegistrar);
        } else if (rawType == EventHandler.class) {
          registerEventHandlerInterface(handlerInstance, parameterizedType, eventRegistrar);
        }
      }
    }
  }

  private static void registerCommandHandlerInterface(
      Object handlerInstance,
      ParameterizedType parameterizedType,
      BiConsumer<Class<?>, CommandHandler<?, ?>> registrar) {
    var typeArgs = parameterizedType.getActualTypeArguments();
    if (typeArgs.length == 2 && typeArgs[0] instanceof Class<?> commandType) {

      // Validate that the command type is annotated with @Command
      if (!commandType.isAnnotationPresent(Command.class)) {
        log.warn(
            "Command handler {} has parameter type {} that is not annotated with @Command",
            handlerInstance.getClass().getSimpleName(),
            commandType.getSimpleName());
        return;
      }

      var handler = (CommandHandler<?, ?>) handlerInstance;
      registrar.accept(commandType, handler);

      log.debug(
          "Registered interface-based command handler: {} for {}",
          handlerInstance.getClass().getSimpleName(),
          commandType.getSimpleName());
    }
  }

  private static void registerQueryHandlerInterface(
      Object handlerInstance,
      ParameterizedType parameterizedType,
      BiConsumer<Class<?>, QueryHandler<?, ?>> registrar) {
    var typeArgs = parameterizedType.getActualTypeArguments();
    if (typeArgs.length == 2 && typeArgs[0] instanceof Class<?> queryType) {

      // Validate that the query type is annotated with @Query
      if (!queryType.isAnnotationPresent(Query.class)) {
        log.warn(
            "Query handler {} has parameter type {} that is not annotated with @Query",
            handlerInstance.getClass().getSimpleName(),
            queryType.getSimpleName());
        return;
      }

      var handler = (QueryHandler<?, ?>) handlerInstance;
      registrar.accept(queryType, handler);

      log.debug(
          "Registered interface-based query handler: {} for {}",
          handlerInstance.getClass().getSimpleName(),
          queryType.getSimpleName());
    }
  }

  private static void registerEventHandlerInterface(
      Object handlerInstance,
      ParameterizedType parameterizedType,
      BiConsumer<Class<?>, EventHandler<?>> registrar) {
    var typeArgs = parameterizedType.getActualTypeArguments();
    if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> eventType) {

      // Validate that the event type is annotated with @Event
      if (!eventType.isAnnotationPresent(Event.class)) {
        log.warn(
            "Event handler {} has parameter type {} that is not annotated with @Event",
            handlerInstance.getClass().getSimpleName(),
            eventType.getSimpleName());
        return;
      }

      var handler = (EventHandler<?>) handlerInstance;
      registrar.accept(eventType, handler);

      log.debug(
          "Registered interface-based event handler: {} for {}",
          handlerInstance.getClass().getSimpleName(),
          eventType.getSimpleName());
    }
  }
}
