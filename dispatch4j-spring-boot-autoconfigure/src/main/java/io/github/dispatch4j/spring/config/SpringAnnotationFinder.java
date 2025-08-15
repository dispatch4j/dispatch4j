package io.github.dispatch4j.spring.config;

import io.github.dispatch4j.annotation.CommandHandler;
import io.github.dispatch4j.annotation.EventHandler;
import io.github.dispatch4j.annotation.QueryHandler;
import io.github.dispatch4j.util.AnnotationFinder;
import java.lang.reflect.Method;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Spring-enhanced implementation of AnnotationUtils using Spring's AnnotationUtils.
 *
 * <p>This implementation is located in
 * dispatch4j-spring-boot-autoconfigure/src/main/java/io/github/dispatch4j/spring/util/SpringAnnotationUtils.java.
 * It uses Spring's {@link AnnotationUtils#findAnnotation(Method, Class)} for annotation detection.
 * This provides enhanced capabilities including:
 *
 * <ul>
 *   <li>Meta-annotation support (annotations on annotations)
 *   <li>Spring AOP proxy support (finds annotations on target methods)
 *   <li>Inheritance and interface annotation resolution
 *   <li>Bridge method handling
 * </ul>
 */
public class SpringAnnotationFinder implements AnnotationFinder {

    @Override
    public boolean isCommandHandler(Method method) {
        return AnnotationUtils.findAnnotation(method, CommandHandler.class) != null;
    }

    @Override
    public boolean isQueryHandler(Method method) {
        return AnnotationUtils.findAnnotation(method, QueryHandler.class) != null;
    }

    @Override
    public boolean isEventHandler(Method method) {
        return AnnotationUtils.findAnnotation(method, EventHandler.class) != null;
    }

    @Override
    public String getDetectionStrategyName() {
        return "SpringAnnotationUtils";
    }
}
