package ch.carve.undertow;

import java.util.function.Function;

import io.undertow.server.HttpHandler;

public class HandlerChainBuilder {
    private final Function<HttpHandler, HttpHandler> function;

    private HandlerChainBuilder(Function<HttpHandler, HttpHandler> function) {
        if (null == function) {
            throw new IllegalArgumentException("HandlerChain function can't be null");
        }
        this.function = function;
    }

    public static HandlerChainBuilder begin(Function<HttpHandler, HttpHandler> function) {
        return new HandlerChainBuilder(function);
    }

    public HandlerChainBuilder next(Function<HttpHandler, HttpHandler> before) {
        return new HandlerChainBuilder(function.compose(before));
    }

    public HttpHandler complete(HttpHandler handler) {
        return function.apply(handler);
    }
}
