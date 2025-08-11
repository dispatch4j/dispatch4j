package io.github.dispatch4j.standalone;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.dispatch4j.core.Dispatch4j;
import io.github.dispatch4j.core.annotation.CommandHandler;
import io.github.dispatch4j.core.annotation.EventHandler;
import io.github.dispatch4j.core.annotation.QueryHandler;
import io.github.dispatch4j.core.handler.HandlerInvoker;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating {@link Dispatch4j} instances in standalone applications.
 *
 * <p>This factory provides automatic handler discovery by scanning specified packages for classes
 * and methods annotated with {@link CommandHandler}, {@link QueryHandler}, and {@link EventHandler}
 * annotations. It uses ClassGraph for efficient classpath scanning.
 *
 * <p>The factory automatically instantiates handler classes using their default constructors and
 * registers them with the dispatcher. Classes that cannot be instantiated (e.g., missing default
 * constructor, abstract classes) are logged as warnings and skipped.
 *
 * <p>This class is designed for standalone applications that do not use dependency injection
 * frameworks like Spring. For Spring Boot applications, use the spring-boot-starter module instead.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * Dispatch4j dispatcher = Dispatch4jFactory.create("com.example.handlers");
 * }</pre>
 *
 * @see Dispatch4j
 * @see io.github.dispatch4j.core.annotation.CommandHandler
 * @see io.github.dispatch4j.core.annotation.QueryHandler
 * @see io.github.dispatch4j.core.annotation.EventHandler
 */
public final class Dispatch4jFactory {

    private static final Logger log = LoggerFactory.getLogger(Dispatch4jFactory.class);

    private Dispatch4jFactory() {}

    /**
     * Creates a new Dispatch4j instance with automatic handler discovery.
     *
     * <p>Scans the specified packages for handler annotations and registers all found handlers.
     * Uses the {@link java.util.concurrent.ForkJoinPool#commonPool()} for asynchronous operations.
     *
     * @param basePackages the base packages to scan for handlers
     * @return a configured Dispatch4j instance with all discovered handlers registered
     */
    public static Dispatch4j create(String... basePackages) {
        return create(null, basePackages);
    }

    /**
     * Creates a new Dispatch4j instance with automatic handler discovery and custom executor.
     *
     * <p>Scans the specified packages for handler annotations and registers all found handlers.
     * Uses the provided executor for asynchronous operations, or the default if null is passed.
     *
     * @param executor the executor to use for async operations (can be null for default)
     * @param basePackages the base packages to scan for handlers
     * @return a configured Dispatch4j instance with all discovered handlers registered
     */
    public static Dispatch4j create(Executor executor, String... basePackages) {
        var dispatcher = executor != null ? new Dispatch4j(executor) : new Dispatch4j();

        try (ScanResult scanResult =
                new ClassGraph().enableAllInfo().acceptPackages(basePackages).scan()) {

            scanForAnnotatedHandlers(dispatcher, scanResult);
        }

        return dispatcher;
    }

    private static void scanForAnnotatedHandlers(Dispatch4j dispatcher, ScanResult scanResult) {
        // Scan for methods with handler annotations
        registerMethodHandlers(
                scanResult,
                CommandHandler.class,
                HandlerInvoker::createCommandHandler,
                dispatcher::registerCommandHandler);

        registerMethodHandlers(
                scanResult,
                QueryHandler.class,
                HandlerInvoker::createQueryHandler,
                dispatcher::registerQueryHandler);

        registerMethodHandlers(
                scanResult,
                EventHandler.class,
                HandlerInvoker::createEventHandler,
                dispatcher::registerEventHandler);

        // Scan for classes implementing handler interfaces
        //        registerInterfaceBasedHandlers(scanResult, dispatcher);
    }

    private static <T> void registerMethodHandlers(
            ScanResult scanResult,
            Class<? extends Annotation> annotationClass,
            HandlerFactory<T> handlerFactory,
            BiConsumer<Class<?>, T> registrar) {
        scanResult
                .getClassesWithMethodAnnotation(annotationClass.getName())
                .forEach(
                        classInfo ->
                                registerMethodHandlersForClass(
                                        classInfo, annotationClass, handlerFactory, registrar));
    }

    private static <T> void registerMethodHandlersForClass(
            ClassInfo classInfo,
            Class<? extends Annotation> annotationClass,
            HandlerFactory<T> handlerFactory,
            BiConsumer<Class<?>, T> registrar) {
        var handlerType = annotationClass.getSimpleName().replace("Handler", "").toLowerCase();

        var handlerInstance =
                createHandlerInstance(classInfo, "method " + handlerType + " handlers");
        if (handlerInstance != null) {
            var handlerClass = handlerInstance.getClass();

            for (var method : handlerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    var handler = handlerFactory.create(method, handlerInstance);

                    var messageType = method.getParameterTypes()[0];
                    registrar.accept(messageType, handler);
                    log.debug(
                            "Registered method {} handler: {}.{} for {}",
                            handlerType,
                            handlerClass.getSimpleName(),
                            method.getName(),
                            messageType.getSimpleName());
                }
            }
        }
    }

    //    private static void registerInterfaceBasedHandlers(ScanResult scanResult, Dispatch4j
    // dispatcher) {
    //        // Scan for classes implementing CommandHandler interface
    //
    // scanResult.getClassesImplementing(io.github.dispatch4j.core.handler.CommandHandler.class.getName())
    //                .forEach(classInfo -> registerInterfaceHandlerForClass(classInfo,
    // dispatcher));
    //
    //        // Scan for classes implementing QueryHandler interface
    //
    // scanResult.getClassesImplementing(io.github.dispatch4j.core.handler.QueryHandler.class.getName())
    //                .forEach(classInfo -> registerInterfaceHandlerForClass(classInfo,
    // dispatcher));
    //
    //        // Scan for classes implementing EventHandler interface
    //
    // scanResult.getClassesImplementing(io.github.dispatch4j.core.handler.EventHandler.class.getName())
    //                .forEach(classInfo -> registerInterfaceHandlerForClass(classInfo,
    // dispatcher));
    //    }

    /*    private static void registerInterfaceHandlerForClass(ClassInfo classInfo, Dispatch4j dispatcher) {
        var handlerInstance = createHandlerInstance(classInfo, "interface-based handler");
        if (handlerInstance != null) {
            HandlerRegistration.registerInterfaceBasedHandler(handlerInstance,
                    dispatcher::registerCommandHandler,
                    dispatcher::registerQueryHandler,
                    dispatcher::registerEventHandler);
        }
    }*/

    private static Object createHandlerInstance(ClassInfo classInfo, String handlerType) {
        try {
            var handlerClass = classInfo.loadClass();
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("Failed to register {}: {}", handlerType, classInfo.getName(), e);
            return null;
        }
    }

    @FunctionalInterface
    private interface HandlerFactory<T> {
        T create(Method method, Object handlerInstance);
    }
}
