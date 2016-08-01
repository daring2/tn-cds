package ru.antinform.cds.test;

import com.google.common.base.Stopwatch;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataTotals;
import java.util.List;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static ru.antinform.cds.utils.ProfileUtils.logCall;

@SuppressWarnings("WeakerAccess")
public class QueryTagDataTest {

	final Logger log = getLogger(getClass());

	final Config config;
	final long runTime;
	final List<Long> periods;
	final TagDataService service;

	final Stopwatch queryTime = Stopwatch.createUnstarted();

	public QueryTagDataTest(Context ctx) {
		config = ctx.mainConfig().getConfig("cds.test.QueryTagDataTest");
		runTime = config.getDuration("runTime", MILLISECONDS);
		periods = config.getDurationList("periods", MILLISECONDS);
		service = ctx.tagDataService();
	}

	public void run() throws Exception {
		long end = runTime + curTime();
		while (curTime() <= end) {
			runQueries();
		}
	}

	private void runQueries() throws Exception {
		long time = curTime();
		for (Long period : periods) {
			TagDataTotals result = logCall(queryTime, () ->
				service.selectTotalsByPeriod(time - period, time)
			);
			log.info("query: period={}, time={}, result={}", period, queryTime, result);
		}
	}

	private long curTime() {
		return currentTimeMillis();
	}

	interface Context {
		Config mainConfig();
		TagDataService tagDataService();
	}

}
