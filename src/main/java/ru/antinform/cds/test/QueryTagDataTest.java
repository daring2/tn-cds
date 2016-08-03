package ru.antinform.cds.test;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
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
	final ExecutorService executor;

	public QueryTagDataTest(Context ctx) {
		super(ctx.mainConfig(), "cds.test." + Name);
		service = ctx.tagDataService();
		executor = createExecutor();
	}

	private List<QueryDef> buildQueries() {
		List<Long> periods = config.getDurationList("periods", MILLISECONDS);
		return periods.stream().map(QueryDef::new).collect(toList());
	}

	private ExecutorService createExecutor() {
		return newFixedThreadPool(threadCount + 1, newThreadFactory(Name + "-%d", true));
	}

	public Future<String> start() {
		return executor.submit(this::run);
	}

	public String run() throws Exception {
		long end = runTime + curTime();
		while (curTime() <= end) {
			for (Future<Long> f : submitQueryTasks()) f.get();
		}
		String result = queries.stream().map(q -> {
			long mean = nanoToMillis((long) q.timer.getSnapshot().getMean());
			return format("query-%s: mean=%s", q.period, mean);
		}).collect(joining("\n"));
		log.info("result:\n" + result);
		return result;
	}

	private List<Future<Long>> submitQueryTasks() {
		return generate(() -> executor.submit(this::runQueries)).
			limit(threadCount).collect(toList());
	}

	private long runQueries() throws Exception {
		long time = curTime();
		for (QueryDef q : queries) {
			Timer.Context tc = q.timer.time();
			TagDataTotals result = service.selectTotals(time - q.period, time);
			long et = nanoToMillis(tc.stop());
			log.debug("query: period={}, time={}, result={}", q.period, et, result);
		}
		return curTime() - time;
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
