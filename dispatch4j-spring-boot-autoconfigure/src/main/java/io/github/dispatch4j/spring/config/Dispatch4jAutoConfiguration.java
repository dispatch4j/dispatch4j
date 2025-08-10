package io.github.dispatch4j.spring.config;

import io.github.dispatch4j.core.Dispatch4j;
import io.github.dispatch4j.core.Dispatcher;
import io.github.dispatch4j.spring.SpringHandlerRegistry;
import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

/**
 * Spring Boot auto-configuration for Dispatch4j CQRS library.
 *
 * <p>This configuration automatically sets up the required beans for Dispatch4j when the library is
 * present on the classpath. It creates:
 *
 * <ul>
 *   <li>A {@link SpringHandlerRegistry} for automatic handler discovery
 *   <li>A {@link ThreadPoolTaskExecutor} for async operations
 *   <li>A {@link Dispatch4j} instance configured with the above components
 * </ul>
 *
 * <p>The auto-configuration is enabled by default but can be disabled by setting {@code
 * dispatch4j.enabled=false} in your application properties.
 *
 * <p>Security context delegation is enabled by default when Spring Security is present, ensuring
 * that security context is properly propagated to async operations. This can be disabled by setting
 * {@code dispatch4j.delegate-security-context=false}.
 *
 * @see Dispatch4jProperties
 * @see SpringHandlerRegistry
 * @see Dispatch4j
 */
@AutoConfiguration
@ConditionalOnClass(Dispatcher.class)
@ConditionalOnProperty(
    prefix = "dispatch4j",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(Dispatch4jProperties.class)
public class Dispatch4jAutoConfiguration {

  private static Executor createBasicExecutor(Dispatch4jProperties properties) {
    var executor = new ThreadPoolTaskExecutor();
    var asyncConfig = properties.getAsync();

    executor.setCorePoolSize(asyncConfig.getCorePoolSize());
    executor.setMaxPoolSize(asyncConfig.getMaxPoolSize());
    executor.setQueueCapacity(asyncConfig.getQueueCapacity());
    executor.setThreadNamePrefix(asyncConfig.getThreadNamePrefix());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();

    return executor;
  }

  /**
   * Creates the default executor for async Dispatch4j operations.
   *
   * <p>This bean is only created if no other bean named "dispatch4jExecutor" is present. The
   * executor is configured using the properties from {@link Dispatch4jProperties.Async}.
   *
   * @param properties the Dispatch4j configuration properties
   * @return a configured ThreadPoolTaskExecutor
   */
  @Bean
  @ConditionalOnMissingBean(name = "dispatch4jExecutor")
  public Executor dispatch4jExecutor(Dispatch4jProperties properties) {
    return createBasicExecutor(properties);
  }

  /**
   * Creates the Spring handler registry for automatic handler discovery.
   *
   * <p>This bean is only created if no other SpringHandlerRegistry bean is present. The registry
   * will automatically discover and register handlers during Spring application context
   * initialization.
   *
   * @return a new SpringHandlerRegistry instance
   */
  @Bean
  @ConditionalOnMissingBean(SpringHandlerRegistry.class)
  public SpringHandlerRegistry springHandlerRegistry() {
    return new SpringHandlerRegistry();
  }

  /**
   * Creates the main Dispatch4j instance.
   *
   * <p>This bean is only created if no other Dispatcher bean is present. It combines the
   * SpringHandlerRegistry with the configured executor to create a fully functional dispatcher.
   *
   * @param properties the Dispatch4j configuration properties
   * @param handlerRegistry the Spring handler registry
   * @return a configured Dispatch4j instance
   */
  @Bean
  @ConditionalOnMissingBean(Dispatcher.class)
  public Dispatch4j dispatch4j(
      Dispatch4jProperties properties, SpringHandlerRegistry handlerRegistry) {

    // Create the executor directly to avoid ambiguity
    var executor = dispatch4jExecutor(properties);

    return new Dispatch4j(handlerRegistry, executor);
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(DelegatingSecurityContextExecutor.class)
  @ConditionalOnProperty(
      prefix = "dispatch4j",
      name = "delegate-security-context",
      havingValue = "true",
      matchIfMissing = true)
  static class SecurityContextDelegatingConfiguration {

    @Bean(name = "dispatch4jExecutor")
    public Executor dispatch4jSecurityDelegatingExecutor(Dispatch4jProperties properties) {
      var executor = createBasicExecutor(properties);
      return new DelegatingSecurityContextExecutor(executor);
    }
  }
}
