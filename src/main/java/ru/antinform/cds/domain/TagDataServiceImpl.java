package ru.antinform.cds.domain;

import com.codahale.metrics.Timer;
import com.datastax.driver.core.*;
import com.typesafe.config.Config;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import ru.antinform.cds.utils.StreamUtils;
import java.util.List;
import java.util.stream.Stream;
import static com.datastax.driver.core.BatchStatement.Type.LOGGED;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.IntStream.range;
import static ru.antinform.cds.metrics.MetricUtils.meterCall;

@SuppressWarnings("WeakerAccess")
public class TagDataServiceImpl extends BaseBean implements TagDataService {

	final Metrics metrics = new Metrics();

	final long datePeriod = config.getDuration("datePeriod", MILLISECONDS);
	final Session session;
	final PreparedStatement insertStat;
	final PreparedStatement findByPeriodStat;
	final PreparedStatement selectTotalsStat;

	public TagDataServiceImpl(Context ctx) {
		super(ctx.mainConfig(), "cds.TagDataService");
		session = ctx.session();
		if (config.getBoolean("createTable"))
			session.execute(config.getString("createTableSql"));
		insertStat = prepareStatement("insert");
		findByPeriodStat = prepareStatement("findByPeriod");
		selectTotalsStat = prepareStatement("selectTotals");
	}

	private PreparedStatement prepareStatement(String key) {
		return session.prepare(config.getString(key + "Sql"));
	}

	public ResultSetFuture saveAll(List<TagData> data) {
		log.debug("saveAll(data.size={})", data.size());
		return meterCall(metrics.saveAllAsync, () -> {
			Timer.Context tc = metrics.saveAll.time();
			BatchStatement batch = newBatchStatement();
			for (TagData d : data)
				batch.add(insertStat.bind(d.tag, calcDate(d.time), d.time, d.value, d.quality));
			ResultSetFuture rf = session.executeAsync(batch);
			rf.addListener(tc::stop, directExecutor());
			return rf;
		});
	}

	private BatchStatement newBatchStatement() {
		BatchStatement batch = new BatchStatement(LOGGED);
		batch.setIdempotent(true);
		return batch;
	}

	private int calcDate(long time) {
		return (int) (time / datePeriod);
	}

	public Stream<TagData> findByPeriod(long start, long end) {
		log.debug("findByPeriod(start={}, end={})", start, end);
		return meterCall(metrics.findByPeriod, () -> {
			Stream<ResultSet> rs = selectByPeriod(findByPeriodStat, start, end);
			return rs.flatMap(StreamUtils::stream).map(TagData::read);
		});
	}

	@Override
	public long selectLastTime() {
		return session.execute("select time from tag_data limit 1").one().getLong(0);
	}

	public TagDataTotals selectTotals(long start, long end) {
		log.debug("selectTotals(start={}, end={})", start, end);
		return meterCall(metrics.selectTotals, () -> {
			TagDataTotals result = new TagDataTotals();
			Stream<ResultSet> rs = selectByPeriod(selectTotalsStat, start, end);
			rs.map(ResultSet::one).forEach(result::add);
			return result;
		});
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

	static class Metrics {
		final MetricBuilder mb = new MetricBuilder("TagDataService");
		final Timer saveAll = mb.timer("saveAll");
		final Timer saveAllAsync = mb.timer("saveAllAsync");
		final Timer findByPeriod = mb.timer("findByPeriod");
		final Timer selectTotals = mb.timer("selectTotals");
	}

}

