package ch.carve.undertow;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.Undertow.ListenerInfo;
import io.undertow.server.ConnectorStatistics;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class DelayedHelloWorldHandler implements HttpHandler {

    private static Logger logger = LoggerFactory.getLogger(DelayedHelloWorldHandler.class);

    private Function<Integer, ListenerInfo> listenerInfoGetter;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Thread.sleep(50);
        logger.info("Current active connections: {}", getConnectorStatistics().getActiveConnections());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World delayed");
    }

    private ConnectorStatistics getConnectorStatistics() {
        return listenerInfoGetter.apply(0).getConnectorStatistics();
    }

    public void setListenerInfoGetter(Function<Integer, ListenerInfo> object) {
        listenerInfoGetter = object;
    }

}
