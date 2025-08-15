package io.github.dispatch4j.spring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.dispatch4j.Dispatch4j;
import io.github.dispatch4j.spring.utils.SpringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class Dispatch4jCustomizersTest {

    private final ApplicationContextRunner contextRunner = SpringUtils.createContextRunner();

    @Test
    void shouldCustomizeDispatch4jWithRealUseCaseScenario() {
        contextRunner
                .withUserConfiguration(TestConfiguration.class)
                .run(
                        context -> {
                            // Verify all beans are present
                            assertThat(context)
                                    .hasSingleBean(Dispatch4j.class)
                                    .hasSingleBean(HandlerRegistryCustomizer.class)
                                    .hasSingleBean(MiddlewareChainCustomizer.class);

                            // Verify customizations
                            verifyCustomizerExecuted(context, HandlerRegistryCustomizer.class);
                            verifyCustomizerExecuted(context, MiddlewareChainCustomizer.class);
                        });
    }

    private <T extends Customizer<C>, C> void verifyCustomizerExecuted(
            ApplicationContext ctx, Class<T> customizerClass) {
        var customizer = ctx.getBean(customizerClass);
        verify(customizer, times(1)).customize(any());
    }

    // Test configuration that demonstrates real-world usage

    @Configuration(proxyBeanMethods = false)
    private static class TestConfiguration {

        @Bean
        public HandlerRegistryCustomizer handlerRegistryCustomizer() {
            return mock(HandlerRegistryCustomizer.class);
        }

        @Bean
        public MiddlewareChainCustomizer middlewareChainCustomizer() {
            return mock(MiddlewareChainCustomizer.class);
        }
    }
}
