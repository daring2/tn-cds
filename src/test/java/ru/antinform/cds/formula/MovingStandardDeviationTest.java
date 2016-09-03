package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.assertEquals;

public class MovingStandardDeviationTest {

	private static final double delta = 0.001;

	@Test
	public void testApply() throws Exception {
		MovingStandardDeviation f = new MovingStandardDeviation(5);
		DescriptiveStatistics ds = new DescriptiveStatistics(5);
		new Random(123).doubles(100).map(v -> v *100).forEach(v -> {
			ds.addValue(v);
			assertEquals(ds.getStandardDeviation(), f.apply(v), delta);
		});
	}

}