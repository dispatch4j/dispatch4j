package io.github.dispatch4j.spring.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

class Dispatch4jPropertiesTest {

  @Test
  void shouldHaveDefaultValues() {
    // given
    var properties = new Dispatch4jProperties();

    // when
    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.isDelegateSecurityContext()).isTrue();
    assertThat(properties.getCustomExecutorBeanName()).isNull();

    // then
    var async = properties.getAsync();
    assertThat(async.getCorePoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors());
    assertThat(async.getMaxPoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
    assertThat(async.getQueueCapacity()).isEqualTo(1000);
    assertThat(async.getThreadNamePrefix()).isEqualTo("dispatch4j-");
  }

  @Test
  void shouldBindPropertiesFromConfiguration() {
    // Given
    var propertyMap =
        Map.<String, Object>of(
            "dispatch4j.enabled",
            false,
            "dispatch4j.delegate-security-context",
            false,
            "dispatch4j.custom-executor-bean-name",
            "myExecutor",
            "dispatch4j.async.core-pool-size",
            5,
            "dispatch4j.async.max-pool-size",
            15,
            "dispatch4j.async.queue-capacity",
            200,
            "dispatch4j.async.thread-name-prefix",
            "custom-");

    // When
    var properties = bindProperties(propertyMap);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.isDelegateSecurityContext()).isFalse();
    assertThat(properties.getCustomExecutorBeanName()).isEqualTo("myExecutor");

    var async = properties.getAsync();
    assertThat(async.getCorePoolSize()).isEqualTo(5);
    assertThat(async.getMaxPoolSize()).isEqualTo(15);
    assertThat(async.getQueueCapacity()).isEqualTo(200);
    assertThat(async.getThreadNamePrefix()).isEqualTo("custom-");
  }

  @Test
  void shouldAllowPartialConfiguration() {
    var propertyMap =
        Map.<String, Object>of("dispatch4j.enabled", false, "dispatch4j.async.core-pool-size", 8);

    var properties = bindProperties(propertyMap);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.isDelegateSecurityContext()).isTrue(); // default
    assertThat(properties.getCustomExecutorBeanName()).isNull(); // default

    var async = properties.getAsync();
    assertThat(async.getCorePoolSize()).isEqualTo(8); // custom
    assertThat(async.getMaxPoolSize())
        .isEqualTo(Runtime.getRuntime().availableProcessors() * 2); // default
    assertThat(async.getQueueCapacity()).isEqualTo(1000); // default
    assertThat(async.getThreadNamePrefix()).isEqualTo("dispatch4j-"); // default
  }

  @Test
  void shouldAllowSettersToWork() {
    var properties = new Dispatch4jProperties();
    var async = new Dispatch4jProperties.Async();

    properties.setEnabled(false);
    properties.setDelegateSecurityContext(false);
    properties.setCustomExecutorBeanName("testExecutor");

    async.setCorePoolSize(3);
    async.setMaxPoolSize(12);
    async.setQueueCapacity(150);
    async.setThreadNamePrefix("test-");

    properties.setAsync(async);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.isDelegateSecurityContext()).isFalse();
    assertThat(properties.getCustomExecutorBeanName()).isEqualTo("testExecutor");

    var resultAsync = properties.getAsync();
    assertThat(resultAsync.getCorePoolSize()).isEqualTo(3);
    assertThat(resultAsync.getMaxPoolSize()).isEqualTo(12);
    assertThat(resultAsync.getQueueCapacity()).isEqualTo(150);
    assertThat(resultAsync.getThreadNamePrefix()).isEqualTo("test-");
  }

  @Test
  void shouldHandleEmptyConfiguration() {
    var properties = bindProperties(Map.of());

    // Should use all defaults
    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.isDelegateSecurityContext()).isTrue();
    assertThat(properties.getCustomExecutorBeanName()).isNull();

    var async = properties.getAsync();
    assertThat(async.getCorePoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors());
    assertThat(async.getMaxPoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
    assertThat(async.getQueueCapacity()).isEqualTo(1000);
    assertThat(async.getThreadNamePrefix()).isEqualTo("dispatch4j-");
  }

  private Dispatch4jProperties bindProperties(Map<String, Object> properties) {
    var propertySources = new MutablePropertySources();
    propertySources.addFirst(new MapPropertySource("test", properties));

    var binder = new Binder(ConfigurationPropertySources.from(propertySources));
    return binder.bindOrCreate("dispatch4j", Dispatch4jProperties.class);
  }
}
