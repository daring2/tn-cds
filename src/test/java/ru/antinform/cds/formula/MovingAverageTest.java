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
import static org.junit.Assert.assertEquals;

public class MovingAverageTest {

	private static final double delta = 0.001;

	@Test
	public void testApply() throws Exception {
		MovingAverage f = new MovingAverage(3);
		double[][] vs = new double[][] {{1, 1}, {2, 1.5}, {3, 2}, {4, 3}};
		for (double[] vt : vs)
			assertEquals(vt[1], f.apply(vt[0]), delta);
	}

	@Test @Ignore
	public void testSpeed() throws Exception {
		int window = 500;
		List<Formula> fs = range(0, 10000).
			mapToObj(i -> i < 5000 ? new MovingAverage(window) : new StdDeviation(window)).
			collect(toList());
		Stopwatch sw = createStarted();
		int valueCount = 600;
		range(0, valueCount).forEach(i -> {
			for (Formula f : fs)
				f.apply(i);
		});
		sw.stop();
		long t1 = sw.elapsed(MILLISECONDS) / valueCount;
		System.out.println(format("time=%s, t1=%s", sw, t1));
	}

}