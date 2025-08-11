package io.github.dispatch4j.spring;

import io.github.dispatch4j.core.annotation.CommandHandler;
import io.github.dispatch4j.core.annotation.EventHandler;
import io.github.dispatch4j.core.annotation.QueryHandler;
import io.github.dispatch4j.core.handler.HandlerInvoker;
import io.github.dispatch4j.core.handler.HandlerRegistry;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

/**
 * Spring-aware handler registry that automatically discovers and registers handlers.
 *
 * <p>This class extends {@link HandlerRegistry} and implements Spring's {@link BeanPostProcessor}
 * to automatically discover handler methods in Spring beans during application context
 * initialization. It scans for methods annotated with {@link CommandHandler}, {@link QueryHandler},
 * and {@link EventHandler} annotations.
 *
 * <p>Handler discovery process:
 *
 * <ul>
 *   <li>Examines all Spring beans after initialization
 *   <li>Finds methods annotated with handler annotations
 *   <li>Validates method signatures (exactly one parameter, correct return type)
 *   <li>Creates {@link io.github.dispatch4j.core.handler.HandlerInvoker} wrappers
 *   <li>Registers handlers with the underlying registry
 * </ul>
 *
 * <p>This registry also supports interface-based handlers where classes implement the handler
 * interfaces directly.
 *
 * @see HandlerRegistry
 * @see BeanPostProcessor
 * @see io.github.dispatch4j.core.annotation.CommandHandler
 * @see io.github.dispatch4j.core.annotation.QueryHandler
 * @see io.github.dispatch4j.core.annotation.EventHandler
 */
public class SpringHandlerRegistry extends HandlerRegistry implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SpringHandlerRegistry.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, @NonNull String beanName)
            throws BeansException {
        var beanClass = bean.getClass();

        // Register annotated methods
        for (Method method : beanClass.getMethods()) {
            // Check for command handler methods
            if (AnnotationUtils.findAnnotation(method, CommandHandler.class) != null) {
                registerCommandHandler(bean, method);
            }

            // Check for query handler methods
            if (AnnotationUtils.findAnnotation(method, QueryHandler.class) != null) {
                registerQueryHandler(bean, method);
            }

            // Check for event handler methods
            if (AnnotationUtils.findAnnotation(method, EventHandler.class) != null) {
                registerEventHandler(bean, method);
            }
        }

        return bean;
    }

    private void registerCommandHandler(Object bean, Method method) {
        var handler = HandlerInvoker.createCommandHandler(method, bean);

        var commandType = method.getParameterTypes()[0];
        registerCommandHandler(commandType, handler);

        var returnType = method.getReturnType();
        log.debug(
                "Registered Spring command handler method: {}.{} for {} -> {}",
                bean.getClass().getSimpleName(),
                method.getName(),
                commandType.getSimpleName(),
                returnType.getSimpleName());
    }

    private void registerQueryHandler(Object bean, Method method) {
        var handler = HandlerInvoker.createQueryHandler(method, bean);

        var queryType = method.getParameterTypes()[0];
        registerQueryHandler(queryType, handler);

        var returnType = method.getReturnType();
        log.debug(
                "Registered Spring query handler method: {}.{} for {} -> {}",
                bean.getClass().getSimpleName(),
                method.getName(),
                queryType.getSimpleName(),
                returnType.getSimpleName());
    }

    private void registerEventHandler(Object bean, Method method) {
        var handler = HandlerInvoker.createEventHandler(method, bean);

        var eventType = method.getParameterTypes()[0];
        registerEventHandler(eventType, handler);

        log.debug(
                "Registered Spring event handler method: {}.{} for {}",
                bean.getClass().getSimpleName(),
                method.getName(),
                eventType.getSimpleName());
    }
}
