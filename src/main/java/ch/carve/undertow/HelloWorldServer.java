package ch.carve.undertow;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.ListenerInfo;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;

public class HelloWorldServer {
    private static Undertow server = null;
    static final MetricRegistry metricRegistry = new MetricRegistry();

    public static void main(final String[] args) {
        metricRegistry.register("gc", new GarbageCollectorMetricSet());
        metricRegistry.register("memory", new MemoryUsageGaugeSet());
        metricRegistry.register("thread", new ThreadStatesGaugeSet());

        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);

        // TODO try to add connector statistic to logger (through attribute)
        DelayedHelloWorldHandler myHandler = new DelayedHelloWorldHandler();
        myHandler.setListenerInfoGetter(HelloWorldServer::getListenerInfo);
        server = Undertow.builder()
                .setWorkerThreads(25)
                .addHttpListener(8080, "localhost")
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .setHandler(Handlers.routing()
                        .get("/test", HelloWorldHandler::handleRequest)
                        .get("/test2", wrapWithCommonHandlers(HelloWorldHandler::handleRequest))
                        .get("/delayed", wrapWithCommonHandlers(myHandler)))
                .build();

        metricRegistry.register("undertow", new UndertowGaugeSet(server));

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

    private static ListenerInfo getListenerInfo(int index) {
        return server.getListenerInfo().get(index);
    }

}
