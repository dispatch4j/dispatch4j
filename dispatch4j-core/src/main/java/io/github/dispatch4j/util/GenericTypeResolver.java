package io.github.dispatch4j.util;

import io.github.dispatch4j.exception.TypeResolutionException;
import io.github.dispatch4j.handler.CommandHandler;
import io.github.dispatch4j.handler.EventHandler;
import io.github.dispatch4j.handler.QueryHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for resolving generic type parameters from handler interfaces.
 *
 * <p>This utility is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/util/GenericTypeResolver.java. It is used
 * by InterfaceBasedDiscoveryStrategy to determine the message types that handlers can process by
 * examining the generic type parameters of CommandHandler, QueryHandler, and EventHandler
 * interfaces.
 */
public class GenericTypeResolver {

    /**
     * Resolves the command type from a CommandHandler implementation.
     *
     * @param handlerClass the handler class that implements CommandHandler
     * @return the command type (first generic parameter)
     * @throws TypeResolutionException if the command type cannot be resolved
     */
    public Class<?> resolveCommandType(Class<?> handlerClass) {
        return resolveMessageType(handlerClass, CommandHandler.class, 0);
    }

    /**
     * Resolves the query type from a QueryHandler implementation.
     *
     * @param handlerClass the handler class that implements QueryHandler
     * @return the query type (first generic parameter)
     * @throws TypeResolutionException if the query type cannot be resolved
     */
    public Class<?> resolveQueryType(Class<?> handlerClass) {
        return resolveMessageType(handlerClass, QueryHandler.class, 0);
    }

    /**
     * Resolves the event type from an EventHandler implementation.
     *
     * @param handlerClass the handler class that implements EventHandler
     * @return the event type (first generic parameter)
     * @throws TypeResolutionException if the event type cannot be resolved
     */
    public Class<?> resolveEventType(Class<?> handlerClass) {
        return resolveMessageType(handlerClass, EventHandler.class, 0);
    }

    /**
     * Resolves a specific generic type parameter from a handler interface.
     *
     * @param handlerClass the handler class that implements the interface
     * @param interfaceClass the handler interface to examine
     * @param parameterIndex the index of the generic parameter to resolve
     * @return the resolved type
     * @throws TypeResolutionException if the type cannot be resolved
     */
    private Class<?> resolveMessageType(
            Class<?> handlerClass, Class<?> interfaceClass, int parameterIndex) {
        try {
            // First check direct interface implementation
            var directType = findGenericInterface(handlerClass, interfaceClass);
            if (directType != null) {
                return extractTypeArgument(directType, parameterIndex, handlerClass);
            }

            // Check superclasses
            var currentClass = handlerClass.getSuperclass();
            while (currentClass != null && currentClass != Object.class) {
                directType = findGenericInterface(currentClass, interfaceClass);
                if (directType != null) {
                    return extractTypeArgument(directType, parameterIndex, handlerClass);
                }
                currentClass = currentClass.getSuperclass();
            }

            throw new TypeResolutionException(
                    "Could not resolve "
                            + interfaceClass.getSimpleName()
                            + " generic type parameter "
                            + parameterIndex
                            + " for class: "
                            + handlerClass.getName(),
                    handlerClass);

        } catch (RuntimeException e) {
            if (e instanceof TypeResolutionException) {
                throw e;
            }
            throw new TypeResolutionException(
                    "Failed to resolve generic type parameter for "
                            + interfaceClass.getSimpleName()
                            + " in class: "
                            + handlerClass.getName(),
                    handlerClass,
                    e);
        }
    }

    /**
     * Finds the parameterized type for the specified interface in the given class.
     *
     * @param clazz the class to examine
     * @param interfaceClass the interface to find
     * @return the parameterized type, or null if not found
     */
    private Type findGenericInterface(Class<?> clazz, Class<?> interfaceClass) {
        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType().equals(interfaceClass)) {
                    return parameterizedType;
                }
            } else if (genericInterface.equals(interfaceClass)) {
                // Raw type implementation - cannot resolve generics
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts the type argument at the specified index from a parameterized type.
     *
     * @param parameterizedType the parameterized type
     * @param parameterIndex the index of the type argument
     * @param contextClass the class context for error reporting
     * @return the resolved class
     * @throws TypeResolutionException if the type argument cannot be resolved
     */
    private Class<?> extractTypeArgument(
            Type parameterizedType, int parameterIndex, Class<?> contextClass) {
        if (!(parameterizedType instanceof ParameterizedType pt)) {
            throw new TypeResolutionException(
                    "Interface is not parameterized for class: " + contextClass.getName(),
                    contextClass);
        }

        var typeArguments = pt.getActualTypeArguments();
        if (typeArguments.length <= parameterIndex) {
            throw new TypeResolutionException(
                    "Insufficient type arguments (expected at least "
                            + (parameterIndex + 1)
                            + ", found "
                            + typeArguments.length
                            + ") for class: "
                            + contextClass.getName(),
                    contextClass);
        }

        var typeArgument = typeArguments[parameterIndex];

        // Handle different types of type arguments
        if (typeArgument instanceof Class<?> clazz) {
            return clazz;
        } else if (typeArgument instanceof ParameterizedType paramType) {
            // For parameterized types like ComplexCommand<String>, get the raw type
            var rawType = paramType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return clazz;
            }
        }

        throw new TypeResolutionException(
                "Type argument "
                        + parameterIndex
                        + " is not a concrete class or parameterized type (found: "
                        + typeArgument
                        + ") for class: "
                        + contextClass.getName(),
                contextClass);
    }
}
