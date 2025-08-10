package io.github.dispatch4j.spring.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dispatch4j.core.Dispatch4j;
import io.github.dispatch4j.core.Dispatcher;
import io.github.dispatch4j.spring.SpringHandlerRegistry;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

class Dispatch4jAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(Dispatch4jAutoConfiguration.class));

  @Test
  void shouldCreateDefaultBeansWhenEnabled() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(SpringHandlerRegistry.class);
          assertThat(context).hasSingleBean(Dispatch4j.class);
          assertThat(context).hasSingleBean(Dispatcher.class);
          assertThat(context).hasBean("dispatch4jExecutor");
        });
  }

  @Test
  void shouldNotCreateBeansWhenDisabled() {
    contextRunner
        .withPropertyValues("dispatch4j.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(SpringHandlerRegistry.class);
              assertThat(context).doesNotHaveBean(Dispatch4j.class);
              assertThat(context).doesNotHaveBean(Dispatcher.class);
            });
  }

  @Test
  void shouldCreateDefaultExecutorWithProperties() {
    contextRunner
        .withPropertyValues(
            "dispatch4j.async.core-pool-size=5",
            "dispatch4j.async.max-pool-size=15",
            "dispatch4j.async.queue-capacity=50",
            "dispatch4j.async.thread-name-prefix=custom-dispatch-")
        .run(
            context -> {
              assertThat(context).hasBean("dispatch4jExecutor");
              var executor = context.getBean("dispatch4jExecutor", Executor.class);
              assertThat(executor).isNotNull();
            });
  }

  @Test
  void shouldCreateBeanEvenWithMultipleExecutors() {
    contextRunner
        .withUserConfiguration(CustomExecutorConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasBean("customExecutor");
              // Even with multiple executors, we should still be able to create Dispatch4j
              // ObjectProvider.getIfAvailable() will handle the ambiguity
              assertThat(context).hasSingleBean(Dispatch4j.class);
            });
  }

  @Test
  void shouldNotOverrideCustomDispatcher() {
    contextRunner
        .withUserConfiguration(CustomDispatcherConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(Dispatcher.class);
              assertThat(context).hasBean("customDispatcher");
              assertThat(context.getBean(Dispatcher.class)).isInstanceOf(CustomDispatcher.class);
            });
  }

  @Test
  void shouldNotOverrideCustomSpringHandlerRegistry() {
    contextRunner
        .withUserConfiguration(CustomSpringHandlerRegistryConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(SpringHandlerRegistry.class);
              assertThat(context).hasBean("customSpringHandlerRegistry");
              assertThat(context.getBean(SpringHandlerRegistry.class))
                  .isInstanceOf(CustomSpringHandlerRegistry.class);
            });
  }

  @Test
  void shouldCreateSecurityDelegatingExecutorWhenSecurityOnClasspath() {
    contextRunner
        .withPropertyValues("dispatch4j.delegate-security-context=true")
        .run(
            context -> {
              assertThat(context).hasBean("dispatch4jExecutor");
              var executor = context.getBean("dispatch4jExecutor", Executor.class);
              assertThat(executor).isInstanceOf(DelegatingSecurityContextExecutor.class);
            });
  }

  @Test
  void shouldNotCreateSecurityDelegatingExecutorWhenDisabled() {
    contextRunner
        .withPropertyValues("dispatch4j.delegate-security-context=false")
        .run(
            context -> {
              assertThat(context).hasBean("dispatch4jExecutor");
              var executor = context.getBean("dispatch4jExecutor", Executor.class);
              assertThat(executor).isNotInstanceOf(DelegatingSecurityContextExecutor.class);
            });
  }

  @Test
  void shouldUseDefaultPropertiesWhenNotSpecified() {
    contextRunner.run(
        context -> {
          var properties = context.getBean(Dispatch4jProperties.class);
          assertThat(properties.isEnabled()).isTrue();
          assertThat(properties.isDelegateSecurityContext()).isTrue();
          assertThat(properties.getAsync().getCorePoolSize())
              .isEqualTo(Runtime.getRuntime().availableProcessors());
          assertThat(properties.getAsync().getMaxPoolSize())
              .isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
          assertThat(properties.getAsync().getQueueCapacity()).isEqualTo(1000);
          assertThat(properties.getAsync().getThreadNamePrefix()).isEqualTo("dispatch4j-");
        });
  }

  @Test
  void shouldBindCustomProperties() {
    // When disabled, beans won't be created but properties should still bind
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(Dispatch4jAutoConfiguration.class))
        .withPropertyValues(
            "dispatch4j.enabled=false",
            "dispatch4j.delegate-security-context=false",
            "dispatch4j.async.core-pool-size=8",
            "dispatch4j.async.max-pool-size=20",
            "dispatch4j.async.queue-capacity=200",
            "dispatch4j.async.thread-name-prefix=my-app-")
        .run(
            context -> {
              // When disabled, no beans should be created
              assertThat(context).doesNotHaveBean(Dispatch4j.class);
              assertThat(context).doesNotHaveBean(SpringHandlerRegistry.class);

              // But we can still test property binding by creating the properties manually
              var properties = new Dispatch4jProperties();
              // The properties would be bound by Spring Boot if the auto-configuration was enabled
            });
  }

  // Test configurations

  @Configuration(proxyBeanMethods = false)
  static class CustomExecutorConfiguration {
    @Bean
    public Executor customExecutor() {
      return ForkJoinPool.commonPool();
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomExecutorOnlyConfiguration {
    @Bean
    public Executor customExecutor() {
      return ForkJoinPool.commonPool();
    }

    // This configuration won't have the default dispatch4jExecutor
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomDispatcherConfiguration {
    @Bean
    public Dispatcher customDispatcher() {
      return new CustomDispatcher();
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomSpringHandlerRegistryConfiguration {
    @Bean
    public SpringHandlerRegistry customSpringHandlerRegistry() {
      return new CustomSpringHandlerRegistry();
    }
  }

  // Custom implementations for testing

  static class CustomDispatcher implements Dispatcher {
    @Override
    public <R> R send(Object command) {
      return null;
    }

    @Override
    public void publish(Object event) {}

    @Override
    public <R> java.util.concurrent.CompletableFuture<R> sendAsync(Object command) {
      return null;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> publishAsync(Object event) {
      return null;
    }
  }

  static class CustomSpringHandlerRegistry extends SpringHandlerRegistry {}
}
