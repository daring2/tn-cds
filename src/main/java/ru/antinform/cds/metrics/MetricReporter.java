package ru.antinform.cds.metrics;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Slf4jReporter;
import com.typesafe.config.Config;
import java.io.File;
import java.time.Duration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ru.antinform.cds.metrics.MetricUtils.metricRegistry;

public class MetricReporter {

	private final Config config;

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
			createsObjectNamesWith(new JmxObjectNameFactory()).
			build();
		r.start();
	}

	private void createLogReporter() {
		Slf4jReporter r = Slf4jReporter.forRegistry(metricRegistry()).build();
		Duration period = config.getDuration("log.period");
		r.start(period.toMillis(), MILLISECONDS);
	}

	private void createCsvReporter() {
		File dir = new File(config.getString("csv.dir"));
		CsvReporter r = CsvReporter.forRegistry(metricRegistry()).build(dir);
		Duration period = config.getDuration("csv.period");
		r.start(period.toMillis(), MILLISECONDS);
	}

}
