package ch.carve.undertow;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.metrics.ElasticsearchReporter;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import io.undertow.Handlers;
import io.undertow.Undertow;
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

        // Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
        // .outputTo(LoggerFactory.getLogger("metrics"))
        // .convertRatesTo(TimeUnit.SECONDS)
        // .convertDurationsTo(TimeUnit.MILLISECONDS)
        // .build();
        // reporter.start(30, TimeUnit.SECONDS);
        ElasticsearchReporter reporter;
        try {
            reporter = ElasticsearchReporter.forRegistry(metricRegistry)
                    .hosts("localhost:9200")
                    .index("metrics")
                    .build();
            reporter.start(30, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server = Undertow.builder()
                .setWorkerThreads(25)
                .addHttpListener(8080, "0.0.0.0")
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .setHandler(Handlers.routing()
                        .get("/test", HelloWorldHandler::handleRequest)
                        .get("/test2", wrapWithCommonHandlers(HelloWorldHandler::handleRequest))
                        .get("/delayed", wrapWithCommonHandlers(new DelayedHelloWorldHandler())))
                .build();

        metricRegistry.register("undertow", new UndertowGaugeSet(server));

        server.start();
    }

    private static HttpHandler accessLog(HttpHandler next) {
        Slf4jAccessLogReceiver receiver = new Slf4jAccessLogReceiver(LoggerFactory.getLogger("access-log"));
        return new AccessLogHandler(next, receiver, "%h %m %U %q %s %Dms", HelloWorldServer.class.getClassLoader());
    }

    private static HttpHandler limit(HttpHandler next) {
        return new RequestLimitingHandler(25, 1, next);
    }

    private static HttpHandler wrapWithCommonHandlers(HttpHandler handler) {
        return HandlerChainBuilder.begin(BlockingHandler::new)
                .next(HelloWorldServer::accessLog)
                .next(HelloWorldServer::limit)
                .next(MdcHandler::new)
                .complete(handler);
    }

}
