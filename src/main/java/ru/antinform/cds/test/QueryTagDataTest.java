package ru.antinform.cds.test;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import java.util.List;
import java.util.concurrent.Executor;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static ru.antinform.cds.metrics.MetricUtils.nanoToMillis;
import static ru.antinform.cds.utils.ConcurrentUtils.newThreadFactory;

@SuppressWarnings("WeakerAccess")
public class QueryTagDataTest extends BaseBean {

	final static String Name = "QueryTagDataTest";

	final MetricBuilder mb = new MetricBuilder(Name);
	final long runTime = config.getDuration("runTime", MILLISECONDS);
	final int threadCount = config.getInt("threadCount");
	final List<QueryDef> queries = buildQueries();
	final TagDataService service;
	final Executor executor;

	public QueryTagDataTest(Context ctx) {
		super(ctx.mainConfig(), Name);
		service = ctx.tagDataService();
		executor = createExecutor();
	}

	private List<QueryDef> buildQueries() {
		List<Long> periods = config.getDurationList("periods", MILLISECONDS);
		return periods.stream().map(QueryDef::new).collect(toList());
	}

	private Executor createExecutor() {
		return newFixedThreadPool(threadCount, newThreadFactory(Name + "-%d", true));
	}

	public void run() throws Exception {
		long end = runTime + curTime();
		while (curTime() <= end) {
			runQueries();
		}
		for (QueryDef q : queries) {
			long mean = nanoToMillis((long) q.timer.getSnapshot().getMean());
			log.info("query-{}: mean={}", q.period, mean);
		}
	}

	private void runQueries() throws Exception {
		long time = curTime();
		for (QueryDef q : queries) {
			Timer.Context tc = q.timer.time();
			TagDataTotals result = service.selectTotals(time - q.period, time);
			long et = nanoToMillis(tc.stop());
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
