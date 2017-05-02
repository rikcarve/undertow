package ch.carve.undertow;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import io.undertow.Undertow;
import io.undertow.server.ConnectorStatistics;

public class UndertowGaugeSet implements MetricSet {

    private final Undertow server;

    public UndertowGaugeSet(Undertow server) {
        this.server = server;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put("connections.active", new Gauge<Long>() {
            @Override
            public Long getValue() {
                ConnectorStatistics stats = getConnectorStatistics();
                if (stats == null) {
                    return 0L;
                } else {
                    return stats.getActiveConnections();
                }
            }
        });
        gauges.put("requests.count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                ConnectorStatistics stats = getConnectorStatistics();
                if (stats == null) {
                    return 0L;
                } else {
                    return stats.getRequestCount();
                }
            }
        });
        gauges.put("requests.active", new Gauge<Long>() {
            @Override
            public Long getValue() {
                ConnectorStatistics stats = getConnectorStatistics();
                if (stats == null) {
                    return 0L;
                } else {
                    return stats.getActiveRequests();
                }
            }
        });
        return gauges;
    }

    private ConnectorStatistics getConnectorStatistics() {
        if (server.getListenerInfo() == null || server.getListenerInfo().isEmpty()) {
            return null;
        } else {
            return server.getListenerInfo().get(0).getConnectorStatistics();
        }
    }
}
