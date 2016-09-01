package ru.antinform.cds.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.function.Supplier;
import static java.lang.String.format;
import static ru.antinform.cds.metrics.MetricUtils.Registry;

@SuppressWarnings("WeakerAccess")
public class MetricBuilder {

	public final String component;
	public final MetricRegistry registry;

	public MetricBuilder(String component) {
		this.component = component;
		registry = Registry;
	}

	public String metricName(String name) {
		return format("%s,name=%s", component, name);
	}

	public Timer timer(String name) {
		return registry.timer(metricName(name));
	}

	public Meter meter(String name) {
		return registry.meter(metricName(name));
	}

	public <T> Gauge<T> gauge(String name, Supplier<T> supplier) {
		return registry.register(metricName(name), supplier::get);
	}

}
