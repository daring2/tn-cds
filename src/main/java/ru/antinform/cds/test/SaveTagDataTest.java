package ru.antinform.cds.test;

import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.ListenableFuture;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import ru.antinform.cds.domain.TagData;
import ru.antinform.cds.domain.TagDataService;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("WeakerAccess")
@NotThreadSafe
class SaveTagDataTest {

	final Logger log = getLogger(getClass());

	final Context ctx;
	final Config config;
	final int tagCount;
	final long savePeriod;
	final long runTime;
	final int asyncDelay;

	final BlockingQueue<Future<?>> resultQueue = new LinkedBlockingQueue<>();

	long saveTime;

	SaveTagDataTest(Context ctx) {
		this.ctx = ctx;
		config = ctx.mainConfig().getConfig("cds.test.SaveTagDataTest");
		tagCount = config.getInt("tagCount");
		savePeriod = config.getDuration("savePeriod", MILLISECONDS);
		runTime = config.getDuration("runTime", MILLISECONDS);
		asyncDelay = config.getInt("asyncDelay");
	}

	void run() throws Exception {
		if (config.getBoolean("truncateDataTable"))
			ctx.session().execute("truncate tag_data");
		saveTime = 0;
		int saveCount = (int) (runTime / savePeriod);
		for (int i = 0; i < saveCount; i++) {
			long start = curTime();
			saveValues(start, i);
			if (i >= asyncDelay) resultQueue.take().get();
			long callTime = curTime() - start;
			saveTime += callTime;
			long wait = savePeriod - callTime;
			if (wait > 0) sleep(wait);
		}
		long vps = tagCount * saveCount / (saveTime / 1000);
		log.info("result: tags={}, saves={}, time={}, vps={}", tagCount, saveCount, saveTime, vps);
	}

	private void saveValues(long time, int vi) throws Exception {
		List<TagData> data = new ArrayList<>(tagCount);
		for (int i = 0; i < tagCount; i++)
			data.add(new TagData("t" + i, time, vi + i, 0));
		ListenableFuture<?> rf = ctx.tagDataService().saveAll(data);
		resultQueue.put(rf);
	}

	private long curTime() {
		return currentTimeMillis();
	}

	interface Context {
		Config mainConfig();
		Session session();
		TagDataService tagDataService();
	}

}
