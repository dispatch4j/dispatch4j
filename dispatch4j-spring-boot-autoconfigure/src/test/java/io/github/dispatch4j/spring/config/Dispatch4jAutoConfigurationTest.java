package io.github.dispatch4j.spring.config;

import static io.github.dispatch4j.spring.config.Dispatch4jAutoConfiguration.DISPATCH4J_EXECUTOR_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.dispatch4j.Dispatch4j;
import io.github.dispatch4j.Dispatcher;
import io.github.dispatch4j.discovery.CompositeDiscoveryStrategy;
import io.github.dispatch4j.discovery.HandlerDiscoveryStrategy;
import io.github.dispatch4j.spring.SpringHandlerRegistry;
import io.github.dispatch4j.spring.utils.SpringUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

class Dispatch4jAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = SpringUtils.createContextRunner();

    @Test
    void shouldCreateDefaultBeansWhenEnabled() {
        contextRunner.run(
                context -> {
                    assertThat(context)
                            .hasSingleBean(SpringHandlerRegistry.class)
                            .hasSingleBean(Dispatch4j.class)
                            .hasSingleBean(Dispatcher.class)
                            .hasBean(DISPATCH4J_EXECUTOR_BEAN_NAME);
                });
    }

    @Test
    void shouldNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("dispatch4j.enabled=false")
                .run(
                        context -> {
                            assertThat(context)
                                    .doesNotHaveBean(SpringHandlerRegistry.class)
                                    .doesNotHaveBean(Dispatch4j.class)
                                    .doesNotHaveBean(Dispatcher.class);
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
                            assertThat(context).hasBean(DISPATCH4J_EXECUTOR_BEAN_NAME);
                            var executor =
                                    context.getBean(DISPATCH4J_EXECUTOR_BEAN_NAME, Executor.class);
                            assertThat(executor).isNotNull();
                        });
    }

    @Test
    void shouldCreateBeanEvenWithMultipleExecutors() {
        contextRunner
                .withUserConfiguration(CustomExecutorConfiguration.class)
                .run(
                        context -> {
                            assertThat(context)
                                    .hasBean("customExecutor")
                                    .hasSingleBean(Dispatch4j.class);
                        });
    }

    @Test
    void shouldNotOverrideCustomDispatcher() {
        contextRunner
                .withUserConfiguration(CustomDispatcherConfiguration.class)
                .run(
                        context -> {
                            assertThat(context)
                                    .hasSingleBean(Dispatcher.class)
                                    .hasBean("customDispatcher");
                            assertThat(context.getBean(Dispatcher.class))
                                    .isInstanceOf(CustomDispatcher.class);
                        });
    }

    @Test
    void shouldNotOverrideCustomSpringHandlerRegistry() {
        contextRunner
                .withUserConfiguration(CustomSpringHandlerRegistryConfiguration.class)
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(SpringHandlerRegistry.class);
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
                            assertThat(context).hasBean(DISPATCH4J_EXECUTOR_BEAN_NAME);
                            var executor =
                                    context.getBean(DISPATCH4J_EXECUTOR_BEAN_NAME, Executor.class);
                            assertThat(executor)
                                    .isInstanceOf(DelegatingSecurityContextExecutor.class);
                        });
    }

    @Test
    void shouldNotCreateSecurityDelegatingExecutorWhenDisabled() {
        contextRunner
                .withPropertyValues("dispatch4j.delegate-security-context=false")
                .run(
                        context -> {
                            assertThat(context).hasBean(DISPATCH4J_EXECUTOR_BEAN_NAME);
                            var executor =
                                    context.getBean(DISPATCH4J_EXECUTOR_BEAN_NAME, Executor.class);
                            assertThat(executor)
                                    .isNotInstanceOf(DelegatingSecurityContextExecutor.class);
                        });
    }

    @Test
    void shouldUseDefaultPropertiesWhenNotSpecified() {
        contextRunner.run(
                context -> {
                    var properties = context.getBean(Dispatch4jProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.isDelegateSecurityContext()).isTrue();
                    var async = properties.getAsync();
                    assertThat(async.getCorePoolSize())
                            .isEqualTo(Runtime.getRuntime().availableProcessors());
                    assertThat(async.getMaxPoolSize())
                            .isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
                    assertThat(async.getQueueCapacity()).isEqualTo(1000);
                    assertThat(async.getThreadNamePrefix()).isEqualTo("dispatch4j-");
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
                            assertThat(context)
                                    .doesNotHaveBean(Dispatch4j.class)
                                    .doesNotHaveBean(SpringHandlerRegistry.class);
                        });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomExecutorConfiguration {
        @Bean
        public Executor customExecutor() {
            return Mockito.mock(Executor.class);
        }
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
            return new CustomSpringHandlerRegistry(CompositeDiscoveryStrategy.createDefault());
        }
    }

    static class CustomDispatcher implements Dispatcher {
        @Override
        public <R> R send(Object command) {
            return null;
        }

        @Override
        public void publish(Object event) {}

        @Override
        public <R> CompletableFuture<R> sendAsync(Object command) {
            return null;
        }

        @Override
        public CompletableFuture<Void> publishAsync(Object event) {
            return null;
        }
    }

    static class CustomSpringHandlerRegistry extends SpringHandlerRegistry {
        /**
         * Creates a new SpringHandlerRegistry with the specified discovery strategy.
         *
         * @param discoveryStrategy the discovery strategy to use
         */
        public CustomSpringHandlerRegistry(HandlerDiscoveryStrategy discoveryStrategy) {
            super(discoveryStrategy);
        }
    }
}
