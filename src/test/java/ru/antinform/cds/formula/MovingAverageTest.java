package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.assertEquals;

public class MovingAverageTest {

	private static final double delta = 0.001;

	@Test
	public void testApply() throws Exception {
		MovingAverage f1 = new MovingAverage(3);
		double[][] vs1 = new double[][] {{1, 1}, {2, 1.5}, {3, 2}, {4, 3}};
		for (double[] vt : vs1)
			assertEquals(vt[1], f1.apply(vt[0]), delta);

		MovingAverage f2 = new MovingAverage(5);
		DescriptiveStatistics ds = new DescriptiveStatistics(5);
		new Random(123).doubles(100).forEach(v -> {
			ds.addValue(v);
			assertEquals(ds.getMean(), f2.apply(v), delta);
		});
	}

}