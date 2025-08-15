package io.github.dispatch4j.spring;

import io.github.dispatch4j.discovery.HandlerDiscoveryStrategy;
import io.github.dispatch4j.exception.HandlerDiscoveryException;
import io.github.dispatch4j.handler.HandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * Spring-aware handler registry that automatically discovers and registers handlers using discovery
 * strategies.
 *
 * <p>This class is located in
 * dispatch4j-spring-boot-autoconfigure/src/main/java/io/github/dispatch4j/spring/SpringHandlerRegistry.java.
 * It extends {@link HandlerRegistry} and implements Spring's {@link BeanPostProcessor} to
 * automatically discover handler methods in Spring beans during application context initialization.
 * It uses the parent's discovery strategy infrastructure with Spring-enhanced annotation detection.
 *
 * <p>Handler discovery process:
 *
 * <ul>
 *   <li>Examines all Spring beans after initialization
 *   <li>Uses HandlerDiscoveryStrategy implementations for consistent discovery
 *   <li>Leverages Spring-enhanced annotation detection for meta-annotations and AOP proxies
 *   <li>Registers handlers using the unified registerHandlersIn() entry point
 * </ul>
 *
 * <p>This registry supports both annotation-based and interface-based handlers, using the same
 * discovery strategies as the core registry but with Spring-specific enhancements.
 *
 * @see HandlerRegistry
 * @see BeanPostProcessor
 * @see HandlerDiscoveryStrategy
 */
public class SpringHandlerRegistry extends HandlerRegistry implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SpringHandlerRegistry.class);

    /**
     * Creates a new SpringHandlerRegistry with the specified discovery strategy.
     *
     * @param discoveryStrategy the discovery strategy to use
     */
    public SpringHandlerRegistry(HandlerDiscoveryStrategy discoveryStrategy) {
        super(discoveryStrategy);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NonNull String beanName)
            throws BeansException {

        try {
            // Use the parent's discovery strategy to discover and register handlers
            registerHandlersIn(bean);

        } catch (HandlerDiscoveryException e) {
            log.error("Failed to discover handlers in bean {}: {}", beanName, e.getMessage());
            throw new BeansException("Handler discovery failed for bean: " + beanName, e) {};
        } catch (RuntimeException e) {
            log.error(
                    "Unexpected error during handler discovery in bean {}: {}",
                    beanName,
                    e.getMessage());
            throw new BeansException(
                    "Unexpected handler discovery error for bean: " + beanName, e) {};
        }

        return bean;
    }
}
