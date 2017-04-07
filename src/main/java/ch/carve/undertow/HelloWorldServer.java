package ch.carve.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;

public class HelloWorldServer {

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
                .setWorkerThreads(50)
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.routing()
                        .get("/delayed", new BlockingHandler(new DelayedHelloWorldHandler()))
                        .get("/fast", new HelloWorldHandler()))
                .build();
        server.start();
    }

}
