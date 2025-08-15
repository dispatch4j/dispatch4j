package io.github.dispatch4j.spring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.dispatch4j.middleware.HandlerMiddleware;
import io.github.dispatch4j.middleware.LoggingMiddleware;
import io.github.dispatch4j.middleware.MiddlewareChain;
import io.github.dispatch4j.spring.utils.SpringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MiddlewareAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = SpringUtils.createContextRunner();

    @Test
    void shouldCreateEmptyMiddlewareChainByDefault() {
        contextRunner.run(
                context -> {
                    assertThat(context).hasSingleBean(MiddlewareChain.class);
                    var chain = context.getBean(MiddlewareChain.class);
                    assertThat(chain.isEmpty()).isTrue();
                });
    }

    @Test
    void shouldCreateLoggingMiddlewareWhenEnabled() {
        run(
                true,
                context -> {
                    assertThat(context)
                            .hasSingleBean(LoggingMiddleware.class)
                            .hasSingleBean(MiddlewareChain.class);

                    var chain = context.getBean(MiddlewareChain.class);
                    assertThat(chain.size()).isEqualTo(1);
                });
    }

    @Test
    void shouldIncludeCustomMiddlewareInChain() {
        run(
                CustomMiddlewareConfiguration.class,
                context -> {
                    assertThat(context).hasSingleBean(MiddlewareChain.class);

                    var chain = context.getBean(MiddlewareChain.class);
                    assertThat(chain.size()).isEqualTo(1);
                });
    }

    @Test
    void shouldCombineBuiltInAndCustomMiddleware() {
        run(
                true,
                CustomMiddlewareConfiguration.class,
                context -> {
                    assertThat(context)
                            .hasSingleBean(LoggingMiddleware.class)
                            .hasSingleBean(MiddlewareChain.class);

                    var chain = context.getBean(MiddlewareChain.class);
                    assertThat(chain.size()).isEqualTo(2);
                });
    }

    private void run(
            boolean loggingEnabled,
            ContextConsumer<? super AssertableApplicationContext> consumer) {
        run(loggingEnabled, null, consumer);
    }

    private void run(
            Class<?> userConfig, ContextConsumer<? super AssertableApplicationContext> consumer) {
        run(false, userConfig, consumer);
    }

    private void run(
            boolean loggingEnabled,
            Class<?> userConfig,
            ContextConsumer<? super AssertableApplicationContext> consumer) {
        var runner = contextRunner;
        if (userConfig != null) {
            runner = runner.withUserConfiguration(userConfig);
        }
        runner.withPropertyValues("dispatch4j.middleware.logging-enabled=" + loggingEnabled)
                .run(consumer);
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomMiddlewareConfiguration {

        @Bean
        public HandlerMiddleware customMiddleware() {
            return mock(HandlerMiddleware.class);
        }
    }
}
