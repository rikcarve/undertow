package ch.carve.undertow;

import org.slf4j.LoggerFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;

public class HelloWorldServer {

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
                .setWorkerThreads(25)
                .addHttpListener(8080, "localhost")
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setHandler(Handlers.routing()
                        .get("/test", HelloWorldHandler::handleRequest)
                        .get("/test2", wrapWithCommonHandlers(HelloWorldHandler::handleRequest))
                        .get("/delayed", wrapWithCommonHandlers(DelayedHelloWorldHandler::handleRequest)))
                .build();
        server.start();
    }

    private static HttpHandler accessLog(HttpHandler next) {
        Slf4jAccessLogReceiver receiver = new Slf4jAccessLogReceiver(LoggerFactory.getLogger("access-log"));
        return new AccessLogHandler(next, receiver, "%h %m %U %q %s %Dms", HelloWorldServer.class.getClassLoader());
    }

    private static HttpHandler limit(HttpHandler next) {
        return new RequestLimitingHandler(5, 1, next);
    }
    private static HttpHandler wrapWithCommonHandlers(HttpHandler handler) {
        return HandlerChainBuilder.begin(BlockingHandler::new)
                .next(HelloWorldServer::accessLog)
                .next(HelloWorldServer::limit)
                .complete(handler);
    }
}
