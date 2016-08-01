package ru.antinform.cds.test;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import ru.antinform.cds.metrics.MetricBuilder;
import java.util.List;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("WeakerAccess")
public class QueryTagDataTest {

	final Logger log = getLogger(getClass());
	final MetricBuilder mb = new MetricBuilder("QueryTagDataTest");

	final Config config;
	final long runTime;
	final List<QueryDef> queries;
	final TagDataService service;

	public QueryTagDataTest(Context ctx) {
		config = ctx.mainConfig().getConfig("cds.test.QueryTagDataTest");
		runTime = config.getDuration("runTime", MILLISECONDS);
		queries = buildQueries();
		service = ctx.tagDataService();
	}

	private List<QueryDef> buildQueries() {
		List<Long> periods = config.getDurationList("periods", MILLISECONDS);
		return periods.stream().map(QueryDef::new).collect(toList());
	}

	public void run() throws Exception {
		long end = runTime + curTime();
		while (curTime() <= end) {
			runQueries();
		}
	}

	private void runQueries() throws Exception {
		long time = curTime();
		for (QueryDef q : queries) {
			Timer.Context tc = q.timer.time();
			TagDataTotals result = service.selectTotals(time - q.period, time);
			long et = NANOSECONDS.toMillis(tc.stop());
			log.debug("query: period={}, time={}, result={}", q.period, et, result);
		}
	}

	private long curTime() {
		return currentTimeMillis();
	}

	interface Context {
		Config mainConfig();
		TagDataService tagDataService();
	}

	class QueryDef {
		final long period;
		final Timer timer;

		QueryDef(long period) {
			this.period = period;
			this.timer = mb.timer("query-" + period);
		}
	}

}
