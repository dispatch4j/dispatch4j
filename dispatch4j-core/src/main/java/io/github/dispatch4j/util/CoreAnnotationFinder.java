package io.github.dispatch4j.util;

import io.github.dispatch4j.annotation.CommandHandler;
import io.github.dispatch4j.annotation.EventHandler;
import io.github.dispatch4j.annotation.QueryHandler;
import java.lang.reflect.Method;

/**
 * Core implementation of AnnotationUtils using standard Java reflection.
 *
 * <p>This implementation is located in
 * dispatch4j-core/src/main/java/io/github/dispatch4j/core/util/CoreAnnotationUtils.java. It uses
 * basic {@link Method#isAnnotationPresent(Class)} for annotation detection. It works in all
 * environments but only detects direct annotations (not meta-annotations or Spring AOP proxies).
 */
public class CoreAnnotationFinder implements AnnotationFinder {

    @Override
    public boolean isCommandHandler(Method method) {
        return method.isAnnotationPresent(CommandHandler.class);
    }

    @Override
    public boolean isQueryHandler(Method method) {
        return method.isAnnotationPresent(QueryHandler.class);
    }

    @Override
    public boolean isEventHandler(Method method) {
        return method.isAnnotationPresent(EventHandler.class);
    }

    @Override
    public String getDetectionStrategyName() {
        return "CoreJavaReflection";
    }
}
