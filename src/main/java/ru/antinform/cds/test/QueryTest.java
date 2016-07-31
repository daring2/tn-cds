package ru.antinform.cds.test;

import com.google.common.base.Stopwatch;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import ru.antinform.cds.domain.TagDataService;
import java.util.List;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static ru.antinform.cds.utils.ProfileUtils.logCall;

@SuppressWarnings("WeakerAccess")
public class QueryTest {

	final Logger log = getLogger(getClass());

	final Config config;
	final long runTime;
	final List<Long> periods;
	final TagDataService service;

	final Stopwatch queryTime = Stopwatch.createUnstarted();

	public QueryTest(Context ctx) {
		config = ctx.mainConfig().getConfig("cds.test.QueryTest");
		runTime = config.getDuration("runTime", MILLISECONDS);
		periods = config.getDurationList("periods", MILLISECONDS);
		service = ctx.tagDataService();
	}

	void run() throws Exception {
		long end = runTime + curTime();
		while (curTime() <= end) {
			runQueries();
		}
	}

	private void runQueries() throws Exception {
		long time = curTime();
		for (Long period : periods) {
			Long result = logCall(queryTime, () ->
				service.selectCountByPeriod(time - period, time + 1)
			);
			log.info("query: period={}, result={}, time={}", period, result, queryTime);
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
