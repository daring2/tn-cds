package ru.antinform.cds.test;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import ru.antinform.cds.domain.TagData;
import ru.antinform.cds.domain.TagDataService;
import java.util.stream.Stream;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

class TagDataServiceTest {

	private final Logger log = getLogger(getClass());

	private final Context ctx;
	private final TagDataService service;

	TagDataServiceTest(Context ctx) {
		this.ctx = ctx;
		this.service = ctx.tagDataService();
	}

	void run() {
		long time = service.selectLastTime();
		long endTime = time + MINUTES.toMillis(1);
		long r1 = service.findByPeriod(time + 1, endTime).count();
		if (r1 != 0) throw new RuntimeException("r1 = " + r1);
		Stream<TagData> rs = ctx.tagDataService().findByPeriod(time, endTime).limit(5);
		log.info("result:\n" + rs.map(Object::toString).collect(joining("\n")));
	}

	interface Context {
		Session session();
		TagDataService tagDataService();
	}

}
