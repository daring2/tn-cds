package ru.antinform.cds.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class MetricUtils {

	static final MetricRegistry Registry = new MetricRegistry();

	public static MetricRegistry metricRegistry() {
		return Registry;
	}

	public static <T> T meterCall(Timer timer, Supplier<T> func) {
		try {
			return timer.time(func::get);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private MetricUtils() {
	}
}
