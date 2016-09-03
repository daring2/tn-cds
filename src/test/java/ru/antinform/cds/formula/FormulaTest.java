package ru.antinform.cds.formula;

import com.google.common.base.Stopwatch;
import org.junit.Ignore;
import org.junit.Test;
import java.util.List;
import static com.google.common.base.Stopwatch.createStarted;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class FormulaTest {

	@Test
	@Ignore
	public void testSpeed() throws Exception {
		int window = 500;
		List<Formula> fs = range(0, 10000).
			mapToObj(i -> i < 5000 ? new MovingAverage(window) : new MovingStandardDeviation(window)).
			collect(toList());
		int valueCount = 600;
		Stopwatch sw = createStarted();
		range(0, valueCount).forEach(i -> {
			for (Formula f : fs) f.apply(i);
		});
		sw.stop();
		long t1 = sw.elapsed(MILLISECONDS) / valueCount;
		System.out.println(format("time=%s, t1=%s", sw, t1));
	}

}