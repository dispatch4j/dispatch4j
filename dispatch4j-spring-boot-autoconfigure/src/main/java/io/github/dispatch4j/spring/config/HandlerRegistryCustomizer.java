package io.github.dispatch4j.spring.config;

import io.github.dispatch4j.core.handler.HandlerRegistry;

/**
 * Callback interface for customizing a {@link HandlerRegistry} instance.
 *
 * <p>Implementations of this interface can be registered as beans to customize the auto-configured
 * HandlerRegistry instance before it's used to create the Dispatch4j instance.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Bean
 * public HandlerRegistryCustomizer myRegistryCustomizer() {
 *     return registry -> {
 *         // Register custom handlers programmatically
 *         registry.registerCommandHandler(MyCommand.class, new MyCommandHandler());
 *     };
 * }
 * }</pre>
 *
 * @see io.github.dispatch4j.core.handler.HandlerRegistry
 * @see io.github.dispatch4j.spring.SpringHandlerRegistry
 * @see Dispatch4jAutoConfiguration
 */
@FunctionalInterface
public interface HandlerRegistryCustomizer extends Customizer<HandlerRegistry> {}
