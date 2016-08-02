package ru.antinform.cds.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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

}
