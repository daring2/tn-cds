package ru.antinform.cds.domain;

import com.datastax.driver.core.*;
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
	final PreparedStatement selectCountStat;

	public TagDataServiceImpl(Context ctx) {
		config = ctx.mainConfig().getConfig("cds.TagDataService");
		session = ctx.session();
		if (config.getBoolean("createTable"))
			session.execute(config.getString("createTableSql"));
		datePeriod = config.getDuration("datePeriod", MILLISECONDS);
		insertStat = session.prepare(config.getString("insertSql"));
		selectCountStat = session.prepare(config.getString("selectCountStat"));
	}

	public ResultSetFuture saveAll(List<TagData> data) {
		BatchStatement batch = new BatchStatement(LOGGED);
		batch.setIdempotent(true);
		for (TagData d : data) {
			batch.add(insertStat.bind(d.tag, calcDate(d.time), d.time, d.value, d.quality));
		}
		return session.executeAsync(batch);
	}

	private int calcDate(Date time) {
		return (int) (time.getTime() / datePeriod);
	}

	public List<TagData> findByPeriod(Date start, Date end) {
		throw new UnsupportedOperationException();
	}

	public long selectCountByPeriod(Date start, Date end) {
		int endDate = calcDate(end);
		long result = 0;
		for (int d = calcDate(start); d <= endDate; d++) {
			BoundStatement stat = selectCountStat.bind(d, start, end);
			result += session.execute(stat).one().getLong(0);
		}
		return result;
	}

	public interface Context {
		Config mainConfig();
		Session session();
	}

}

