package io.github.dispatch4j.spring.config;

import io.github.dispatch4j.core.middleware.MiddlewareChain;

/**
 * Callback interface for customizing a {@link MiddlewareChain} instance.
 *
 * <p>Implementations of this interface can be registered as beans to customize the auto-configured
 * MiddlewareChain instance before it's used to create the Dispatch4j instance. This allows for
 * programmatic configuration of middleware order and custom middleware components.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Bean
 * public MiddlewareChainCustomizer myMiddlewareCustomizer() {
 *     return middlewareChain -> {
 *         // Add custom middleware at specific positions
 *         middlewareChain.addMiddleware(0, new SecurityMiddleware());
 *         middlewareChain.addMiddleware(new MetricsMiddleware());
 *     };
 * }
 * }</pre>
 *
 * @see io.github.dispatch4j.core.middleware.MiddlewareChain
 * @see io.github.dispatch4j.core.middleware.HandlerMiddleware
 * @see Dispatch4jAutoConfiguration
 */
@FunctionalInterface
public interface MiddlewareChainCustomizer extends Customizer<MiddlewareChain.Builder> {}
