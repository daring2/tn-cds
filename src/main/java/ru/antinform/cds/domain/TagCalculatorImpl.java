package ru.antinform.cds.domain;

import com.codahale.metrics.Timer;
import com.typesafe.config.Config;
import ru.antinform.cds.formula.Formula;
import ru.antinform.cds.formula.MovingAverage;
import ru.antinform.cds.formula.MovingStandardDeviation;
import ru.antinform.cds.metrics.MetricBuilder;
import ru.antinform.cds.utils.BaseBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static ru.antinform.cds.metrics.MetricUtils.meterCall;

@SuppressWarnings("WeakerAccess")
public class TagCalculatorImpl extends BaseBean implements TagCalculator {

	final Metrics metrics = new Metrics();

	final int windowSize = config.getInt("windowSize");
	final Map<String, Formula> formulas = new HashMap<>();

	public TagCalculatorImpl(Context ctx) {
		super(ctx.mainConfig(), "cds.TagCalculator");
	}

	@Override
	public synchronized List<TagData> calculate(List<TagData> data) {
		return meterCall(metrics.calculate, () -> calculateImpl(data));
	}

	private List<TagData> calculateImpl(List<TagData> data) {
		return data.stream().map(d -> {
			Formula f = getFormula(d.tag);
			return new TagData(d.tag + ".c", d.time, f.apply(d.value), d.quality);
		}).collect(toList());
	}

	private Formula getFormula(String tag) {
		return formulas.computeIfAbsent(tag, tc -> {
			int i = parseInt(substringAfterLast(tc, ".")) % 2;
			return i == 0 ? new MovingAverage(windowSize) : new MovingStandardDeviation(windowSize);
		});
	}

	public interface Context {
		Config mainConfig();
	}

	static class Metrics {
		final MetricBuilder mb = new MetricBuilder("TagCalculator");
		final Timer calculate = mb.timer("calculate");
	}

}
