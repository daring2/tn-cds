package ru.antinform.cds.domain;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import java.util.Date;
import java.util.List;
import static com.datastax.driver.core.BatchStatement.Type.LOGGED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("WeakerAccess")
public class TagDataServiceImpl implements TagDataService {

	final Logger log = getLogger(getClass());

	final Config config;
	final Session session;
	final long datePeriod;
	final PreparedStatement insertStat;

	public TagDataServiceImpl(Context ctx) {
		config = ctx.mainConfig().getConfig("cds.TagDataService");
		session = ctx.session();
		if (config.getBoolean("createTable"))
			session.execute(config.getString("createTableSql"));
		datePeriod = config.getDuration("datePeriod", MILLISECONDS);
		insertStat = session.prepare(config.getString("insertSql"));
	}

	public ResultSetFuture saveAll(List<TagData> data) {
		BatchStatement batch = new BatchStatement(LOGGED);
		batch.setIdempotent(true);
		for (TagData d : data) {
			batch.add(insertStat.bind(d.tag, calcDate(d.time), new Date(d.time), d.value, d.quality));
		}
		return session.executeAsync(batch);
	}

	private int calcDate(long time) {
		return (int) (time / datePeriod);
	}

	public List<TagData> findByPeriod(long start, long end) {
		throw new UnsupportedOperationException();
	}

	public long selectCountByPeriod(long start, long end) {
		throw new UnsupportedOperationException();
	}

	public interface Context {
		Config mainConfig();
		Session session();
	}

}

