package ch.carve.undertow;

import org.jboss.logging.MDC;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public final class MdcHandler implements HttpHandler {
    private volatile HttpHandler handler;

    public MdcHandler(final HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getRequestHeaders().forEach((values) -> {
            if (values.getHeaderName().toString().startsWith("X-MDC")) {
                MDC.put(values.getHeaderName().toString().substring(6), values.toArray()[0]);
            }
        });
        handler.handleRequest(exchange);
    }

}
