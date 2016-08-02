package ru.antinform.cds.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.function.Supplier;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

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

	public static long nanoToMillis(long nanos) {
		return NANOSECONDS.toMillis(nanos);
	}

	private MetricUtils() {
	}
}
