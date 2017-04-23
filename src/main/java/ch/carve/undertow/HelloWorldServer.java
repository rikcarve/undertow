package ch.carve.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.JBossLoggingAccessLogReceiver;

public class HelloWorldServer {

    public static void main(final String[] args) {

        Undertow server = Undertow.builder()
                .setWorkerThreads(25)
                .addHttpListener(8080, "localhost")
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setHandler(Handlers.routing()
                        .get("/delayed", aggregatedHandler(new DelayedHelloWorldHandler()))
                        .get("/fast", new MetricsHandler(new HelloWorldHandler())))
                .build();
        server.start();
    }

    private static HttpHandler aggregatedHandler(HttpHandler handler) {
        HttpHandler result = new BlockingHandler(handler);
        result = new AccessLogHandler(result, new JBossLoggingAccessLogReceiver("accesslog"), "%h %m %U %q %s %Dms", HelloWorldServer.class.getClassLoader());
        return result;
    }
}
