package ru.antinform.cds.test;

public class TestApp {

	public static void main(String[] args) throws Exception {
		try (TestContext ctx = new TestContext()) {
			new SaveTest(ctx).run();
		}
	}

}
