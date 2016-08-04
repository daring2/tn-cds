package ru.antinform.cds.metrics;

import com.codahale.metrics.*;
import com.typesafe.config.Config;
import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ru.antinform.cds.metrics.MetricUtils.metricRegistry;

public class MetricReporter implements AutoCloseable {

	private final Config config;
	private final List<Reporter> reporters = new ArrayList<>();

	public MetricReporter(Config mainConfig) {
		config = mainConfig.getConfig("cds.MetricReporter");
		if (config.getBoolean("jmx.enabled"))
			createJmxReporter();
		if (config.getBoolean("log.enabled"))
			createLogReporter();
		if (config.getBoolean("csv.enabled"))
			createCsvReporter();
	}

	private void createJmxReporter() {
		JmxReporter r = JmxReporter.forRegistry(metricRegistry()).
			inDomain("ru.antinform.cds").
			createsObjectNamesWith(new JmxObjectNameFactory()).
			build();
		reporters.add(r);
		r.start();
	}

	private void createLogReporter() {
		Slf4jReporter r = Slf4jReporter.forRegistry(metricRegistry()).build();
		Duration period = config.getDuration("log.period");
		reporters.add(r);
		r.start(period.toMillis(), MILLISECONDS);
	}

	private void createCsvReporter() {
		File dir = new File(config.getString("csv.dir"));
		if (!dir.exists() && !dir.mkdirs())
			throw new RuntimeException("cannot create dir " + dir);
		CsvReporter r = CsvReporter.forRegistry(metricRegistry()).formatFor(Locale.ROOT).build(dir);
		Duration period = config.getDuration("csv.period");
		reporters.add(r);
		r.start(period.toMillis(), MILLISECONDS);
	}

	@Override
	public void close() throws Exception {
		for (Reporter r : reporters) {
			if (r instanceof ScheduledReporter) ((ScheduledReporter) r).report();
			if (r instanceof Closeable) ((Closeable) r).close();
		};
	}

}
