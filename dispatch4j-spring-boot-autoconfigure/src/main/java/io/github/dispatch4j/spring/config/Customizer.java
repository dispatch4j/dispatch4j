package io.github.dispatch4j.spring.config;

@FunctionalInterface
public interface Customizer<T> {

    void customize(T instance);
}
