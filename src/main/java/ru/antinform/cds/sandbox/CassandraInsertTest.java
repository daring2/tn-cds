package ru.antinform.cds.sandbox;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.common.base.Stopwatch;
import javax.annotation.concurrent.NotThreadSafe;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

/**
 * Possible optimizations:
 * - column types:  date int, value float (-)
 * - unlogged batches (?)
 * - remove tag from partition key (+)
 * - async execution (+)
 * - parallel execution (?)
 *
 * Results (local, tags=1000, values=1000):
 * - initial: 37.59 s, 35.29 s
 * - date key: 13.26 s, 14.25 s
 * - unlogged batches: 13.40 s, 12.88 s
 * - optimal column types: 14.64 s, 14.43 s
 * - async: 9.359 s, 10.16 s
 * - async and parallel (2): 11.50 s, 9.776 s
 *
 * Results (cassandra-144, tags=1000, values=2000):
 * - async: 10.91 s, 11.41 s
 */
@NotThreadSafe
class CassandraInsertTest implements Runnable {

	static final String CreateTableSql = "create table test_data(" +
		"tag text, date text, time timestamp, value double, primary key (date, time, tag)" +
		") with clustering order by (time desc)";

	final Session session;
	int tagCount;
	int valueCount;
	boolean async = true;

	final Stopwatch runTime = Stopwatch.createUnstarted();
	final Stopwatch executeTime = Stopwatch.createUnstarted();
	final CountDownLatch latch = new CountDownLatch(valueCount);
	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	PreparedStatement insertStat;

	long curTime = System.currentTimeMillis();
	String date = dateFormat.format(curTime);

	CassandraInsertTest(Session session, int tagCount, int valueCount) {
		this.session = session;
		this.tagCount = tagCount;
		this.valueCount = valueCount;
	}

	public void run() {
		session.execute("drop table if exists test_data");
		session.execute(CreateTableSql);
		//TODO implement

	}

}
