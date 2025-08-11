package io.github.dispatch4j.spring.utils;

import io.github.dispatch4j.spring.config.Dispatch4jAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public final class SpringUtils {

    private SpringUtils() {
        // Utility class, prevent instantiation
    }

    public static ApplicationContextRunner createContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(Dispatch4jAutoConfiguration.class));
    }
}
