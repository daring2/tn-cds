package ru.antinform.cds.test;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class TestApp {

	private static final Logger log = getLogger(TestApp.class);

	public static void main(String[] args) {
		try (TestContext ctx = new TestContext()) {
			new SaveTest(ctx).run();
			new QueryTest(ctx).run();
		} catch (Exception e) {
			log.error("run error", e);
		}
	}

}
