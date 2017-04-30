package ch.carve.undertow;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MetricsHandler implements HttpHandler {
    private final HttpHandler next;
    private final Counter requests;

    public MetricsHandler(HttpHandler next, MetricRegistry metrics) {
        this.next = next;
        requests = metrics.counter("requests");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        requests.inc();
        next.handleRequest(exchange);
    }

}
