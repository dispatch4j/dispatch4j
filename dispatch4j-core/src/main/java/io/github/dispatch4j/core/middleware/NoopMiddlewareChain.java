package io.github.dispatch4j.core.middleware;

import java.util.function.Function;

class NoopMiddlewareChain implements MiddlewareChain {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public MiddlewareChain addMiddleware(HandlerMiddleware middleware) {
        return MiddlewareChain.builder().add(middleware).build();
    }

    @Override
    public MiddlewareChain addMiddleware(int index, HandlerMiddleware middleware) {
        return MiddlewareChain.builder().add(middleware).build();
    }

    @Override
    public MiddlewareChain removeMiddleware(HandlerMiddleware middleware) {
        return this;
    }

    @Override
    public <T, R> R execute(T message, MiddlewareContext context, Function<T, R> finalHandler) {
        return finalHandler.apply(message);
    }
}
