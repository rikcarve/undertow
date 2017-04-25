package ch.carve.undertow;

import org.jboss.logging.Logger;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class HelloWorldHandler {
    private static final Logger logger = Logger.getLogger(HelloWorldHandler.class);

    public static void handleRequest(HttpServerExchange exchange) throws Exception {
        logger.info(exchange.getRequestPath());
        String name = exchange.getQueryParameters().get("name").getLast();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello " + name);
    }

}
