package ru.antinform.cds.sandbox;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.base.Stopwatch;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static com.datastax.driver.core.BatchStatement.Type.LOGGED;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.*;
import static ru.antinform.cds.utils.ProfileUtils.logVoidCall;

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
 * - int date: 7.511 s, 7.554 s
 * - durable writes: 8.544 s, 8.800 s
 *
 * Results (cassandra-144, tags=1000, values=2000):
 * - async: 10.91 s, 11.41 s
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
class CassandraInsertTest {

	static final String CreateTableSql = "create table test_data(" +
		"tag text, date int, time timestamp, value double, primary key (date, time, tag)" +
		") with clustering order by (time desc)";

	final Session session;
	int tagCount;
	int valueCount;
	boolean async = true;

	final Stopwatch runTime = Stopwatch.createUnstarted();
	final Stopwatch executeTime = Stopwatch.createUnstarted();
	final CountDownLatch latch = new CountDownLatch(valueCount);
	final TimeUnit datePeriod = DAYS;

	PreparedStatement insertStat;
	long curTime;
	int date;

	CassandraInsertTest(Session session, int tagCount, int valueCount) {
		this.session = session;
		this.tagCount = tagCount;
		this.valueCount = valueCount;
	}

	void run() throws Exception {
		session.execute("drop table if exists test_data");
		session.execute(CreateTableSql);
		insertStat = session.prepare("insert into test_data (tag, date, time, value) values(?, ?, ?, ?)");
		curTime =  currentTimeMillis();
		date = (int) datePeriod.convert(curTime, MILLISECONDS);
		logVoidCall(runTime, () -> {
			for (int i = 0; i < valueCount; i++) insertValues(i);
			latch.await();
		});
		long vps = tagCount * valueCount / runTime.elapsed(SECONDS);
		System.out.println(format("run: tags=%s, values=%s, time=%s, vps=%s",
			tagCount, valueCount, runTime, vps));
	}

	private void insertValues(int vi) throws Exception  {
		BatchStatement batch = new BatchStatement(LOGGED);
		long time = curTime + vi;
		for (int i = 0; i < tagCount; i++) {
			double v = vi + i;
			batch.add(insertStat.bind("t" + i, date, new Date(time), v));
		}
		logVoidCall(executeTime, () -> execute(batch));
		if (tagCount < 10 ) System.out.println("execute: time=" + executeTime);
	}

	private void execute(BatchStatement batch) {
		if (async) {
			ResultSetFuture rf = session.executeAsync(batch);
			rf.addListener(latch::countDown, directExecutor());
		} else {
			session.execute(batch);
			latch.countDown();
		}
	}

}
