package ru.antinform.cds.formula;

import org.junit.Test;
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

}