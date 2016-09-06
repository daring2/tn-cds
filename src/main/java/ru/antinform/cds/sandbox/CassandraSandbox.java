package ru.antinform.cds.sandbox;

import ru.antinform.cds.MainContext;

public class CassandraSandbox {

	public static void main(String[] args) throws Exception {
		try (MainContext ctx = new MainContext()) {
			new CassandraInsertTest(ctx.session(), 1000, 1000).run();
		}
	}

}
