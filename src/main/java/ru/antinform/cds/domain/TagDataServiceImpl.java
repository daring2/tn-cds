package ru.antinform.cds.domain;

import com.datastax.driver.core.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import java.util.List;
import java.util.stream.Stream;
import static com.datastax.driver.core.BatchStatement.Type.LOGGED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.IntStream.range;
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
		BatchStatement batch = newBatchStatement();
		for (TagData d : data) {
			batch.add(insertStat.bind(d.tag, calcDate(d.time), d.time, d.value, d.quality));
		}
		return session.executeAsync(batch);
	}

	private BatchStatement newBatchStatement() {
		BatchStatement batch = new BatchStatement(LOGGED);
		batch.setIdempotent(true);
		return batch;
	}

	private int calcDate(long time) {
		return (int) (time / datePeriod);
	}

	public List<TagData> findByPeriod(long start, long end) {
		throw new UnsupportedOperationException();
	}

	public long selectCountByPeriod(long start, long end) {
		Stream<ResultSet> rs = selectByPeriod(selectCountStat, start, end);
		return rs.mapToLong(r -> r.one().getLong(0)).sum();
	}

	private Stream<ResultSet> selectByPeriod(PreparedStatement stat, long start, long end) {
		return range(calcDate(start), calcDate(end) + 1).mapToObj(d ->
			session.execute(stat.bind(d, start, end))
		);
	}

	public interface Context {
		Config mainConfig();
		Session session();
	}

}

