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

	final MetricBuilder mb = new MetricBuilder("QueryTagDataTest");
	final long startDelay = config.getDuration("startDelay", MILLISECONDS);
	final long runTime = config.getDuration("runTime", MILLISECONDS);
	final long statPeriod = config.getDuration("statPeriod", MILLISECONDS);
	final List<Integer> threadCounts = config.getIntList("threadCounts");
	final List<QueryDef> queries = buildQueries();
	final TagDataService service;
	final ExecutorService executor;

	long valueCount = config.getLong("valueCount");

	public QueryTagDataTest(Context ctx, String configPath) {
		super(ctx.mainConfig(), "cds.test." + configPath);
		service = ctx.tagDataService();
		executor = ctx.executor();
	}

	private List<QueryDef> buildQueries() {
		List<Long> limits = config.getLongList("limits");
		return limits.stream().flatMap(l ->
			threadCounts.stream().map(tc -> new QueryDef(l, tc))
		).collect(toList());
	}

	public void setValueCount(long valueCount) {
		this.valueCount = valueCount;
	}

	public Future<String> start() {
		return executor.submit(() -> {
			sleep(startDelay);
			return run();
		});
	}

	public String run() throws Exception {
		if (valueCount == 0) calculateValueCount();
		long end = curTime() + runTime;
		while (curTime() <= end) {
			for (Integer tc : threadCounts) runParallel(tc);
		}
		String result = queries.stream().map(q -> {
			long mean = nanoToMillis((long) q.timer.getSnapshot().getMean());
			return format("%s: meanTime=%s, count=%s", q.name, mean, q.count);
		}).collect(joining("\n"));
		log.info("result:\n" + result);
		return result;
	}

	private void calculateValueCount() {
		long t = service.selectLastTime();
		valueCount = service.selectTotals(t - statPeriod + 1, t).count / statPeriod;
	}

	private void runParallel(int threads) throws Exception {
		long time = service.selectLastTime();
		List<Future<Long>> fs = generate(() ->
			executor.submit(() -> runQueries(time, threads))
		).limit(threads).collect(toList());
		for (Future<Long> f : fs) f.get();
	}

	private long runQueries(long time, int threads) throws Exception {
		long start = curTime();
		queries.stream().filter(q -> q.threads == threads).forEach(q -> {
			Timer.Context tc = q.timer.time();
			long period = q.limit * 1000 / valueCount;
			TagDataTotals result = service.selectTotals(time - period, time);
			long et = nanoToMillis(tc.stop());
			q.count.incrementAndGet();
			log.debug("query: limit={}, time={}, result={}", q.limit, et, result);
		});
		return curTime() - start;
	}

	private long curTime() {
		return currentTimeMillis();
	}

	public interface Context {
		Config mainConfig();
		ExecutorService executor();
		TagDataService tagDataService();
	}

	class QueryDef {
		final long limit;
		final int threads;
		final String name;
		final Timer timer;
		final AtomicLong count = new AtomicLong();

		QueryDef(long limit, int threads) {
			this.limit = limit;
			this.threads = threads;
			this.name = "query-l" + limit + "-t" + threads;
			this.timer = mb.timer(name);
		}
	}

}
