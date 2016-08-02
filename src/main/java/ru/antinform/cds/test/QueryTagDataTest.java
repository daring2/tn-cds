package ru.antinform.cds.test;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import java.util.List;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("WeakerAccess")
public class QueryTagDataTest extends BaseBean {

	final MetricBuilder mb = new MetricBuilder("QueryTagDataTest");

	final long runTime = config.getDuration("runTime", MILLISECONDS);
	final List<QueryDef> queries = buildQueries();
	final TagDataService service;

	public QueryTagDataTest(Context ctx) {
		super(ctx.mainConfig(), "cds.test.QueryTagDataTest");
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
