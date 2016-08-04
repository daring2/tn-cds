package ru.antinform.cds.test;

public class QueryTestApp {

	public static void main(String[] args) throws Exception {
		try (TestContext ctx = new TestContext()) {
			new QueryTagDataTest(ctx, "LongQueryTest").run();
			new QueryTagDataTest(ctx, "ParallelQueryTest").run();
		}
	}

}
