package ch.carve.undertow;

import org.slf4j.LoggerFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;

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
        result = new AccessLogHandler(result, new Slf4jAccessLogReceiver(LoggerFactory.getLogger("access-log")), "%h %m %U %q %s %Dms", HelloWorldServer.class.getClassLoader());
        return result;
    }
}
