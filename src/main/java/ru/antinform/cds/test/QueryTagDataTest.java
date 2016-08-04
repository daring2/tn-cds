package ru.antinform.cds.test;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static ru.antinform.cds.metrics.MetricUtils.nanoToMillis;

@SuppressWarnings("WeakerAccess")
@NotThreadSafe
public class QueryTagDataTest extends BaseBean {

	final static String Name = "QueryTagDataTest";

	final MetricBuilder mb = new MetricBuilder(Name);
	final long startDelay = config.getDuration("startDelay", MILLISECONDS);
	final long runTime = config.getDuration("runTime", MILLISECONDS);
	final List<Integer> threadCounts = config.getIntList("threadCounts");
	final List<QueryDef> queries = buildQueries();
	final TagDataService service;
	final ExecutorService executor;

	public QueryTagDataTest(Context ctx) {
		super(ctx.mainConfig(), "cds.test." + Name);
		service = ctx.tagDataService();
		executor = ctx.executor();
	}

	private List<QueryDef> buildQueries() {
		List<Long> periods = config.getDurationList("periods", MILLISECONDS);
		return periods.stream().flatMap(p ->
			threadCounts.stream().map(tc -> new QueryDef(p, tc))
		).collect(toList());
	}

	public Future<String> start() {
		return executor.submit(() -> {
			sleep(startDelay);
			return run();
		});
	}

	public String run() throws Exception {
		long end = curTime() + runTime;
		while (curTime() <= end) {
			for (Integer tc : threadCounts)
				runParallel(tc);
		}
		String result = queries.stream().map(q -> {
			long mean = nanoToMillis((long) q.timer.getSnapshot().getMean());
			return format("%s: meanTime=%s, count=%s", q.name, mean, q.count);
		}).collect(joining("\n"));
		log.info("result:\n" + result);
		return result;
	}

	private void runParallel(int threads) throws Exception {
		long time = curTime();
		List<Future<Long>> fs = generate(() ->
			executor.submit(() -> runQueries(time, threads))
		).limit(threads).collect(toList());
		for (Future<Long> f : fs) f.get();
	}

	private long runQueries(long time, int threads) throws Exception {
		queries.stream().filter(q -> q.threads == threads).forEach(q -> {
			Timer.Context tc = q.timer.time();
			TagDataTotals result = service.selectTotals(time - q.period, time);
			long et = nanoToMillis(tc.stop());
			q.count.incrementAndGet();
			log.debug("query: period={}, time={}, result={}", q.period, et, result);
		});
		return curTime() - time;
	}

	private long curTime() {
		return currentTimeMillis();
	}

	interface Context {
		Config mainConfig();
		ExecutorService executor();
		TagDataService tagDataService();
	}

	class QueryDef {
		final long period;
		final int threads;
		final String name;
		final Timer timer;
		final AtomicLong count = new AtomicLong();

		QueryDef(long period, int threads) {
			this.period = period;
			this.threads = threads;
			this.name = "query-p" + period + "-t" + threads;
			this.timer = mb.timer(name);
		}
	}

}
