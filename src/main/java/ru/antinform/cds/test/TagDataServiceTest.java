package ru.antinform.cds.test;

import com.datastax.driver.core.Session;
import ru.antinform.cds.domain.TagDataService;
import static java.util.concurrent.TimeUnit.MINUTES;

class TagDataServiceTest {

	final Context ctx;

	TagDataServiceTest(Context ctx) {
		this.ctx = ctx;
	}

	void run() {
		long time = ctx.session().execute("select time from tag_data limit 1").one().getLong(0);
		long endTime = time + MINUTES.toMillis(1);
		long r1 = ctx.tagDataService().findByPeriod(time + 1, endTime).count();
		if (r1 != 0) throw new RuntimeException("r1 = " + r1);
		ctx.tagDataService().findByPeriod(time, endTime).limit(5).forEach(System.out::println);
	}

	interface Context {
		Session session();
		TagDataService tagDataService();
	}

}
