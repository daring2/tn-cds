package ru.antinform.cds.test;

import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import static org.slf4j.LoggerFactory.getLogger;

public class TestApp {

	private static final Logger log = getLogger(TestApp.class);

	public static void main(String[] args) {
		try (TestContext ctx = new TestContext()) {
			List<Future<?>> results = new ArrayList<>();
			results.add(ctx.startQueryTest("QueryOnSaveTest"));
			if (ctx.enabledTests.contains("SaveTest"))
				new SaveTagDataTest(ctx).run();
			ctx.startQueryTest("LongQueryTest").get();
			ctx.startQueryTest("ParallelQueryTest").get();
			for (Future<?> r : results) r.get();
		} catch (Exception e) {
			log.error("run error", e);
		}
	}

}
