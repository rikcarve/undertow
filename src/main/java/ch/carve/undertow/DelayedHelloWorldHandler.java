package ch.carve.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class DelayedHelloWorldHandler {

    public static void handleRequest(HttpServerExchange exchange) throws Exception {
        Thread.sleep(50);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World delayed");
    }

}
