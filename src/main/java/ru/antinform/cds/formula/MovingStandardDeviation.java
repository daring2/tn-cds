package ru.antinform.cds.formula;

import static java.lang.Math.sqrt;

public class MovingStandardDeviation extends WindowFunction {

	private double sum, sum2;

	public MovingStandardDeviation(int size) {
		super(size);
	}

	public double apply(double v) {
		double v0 = values[index];
		sum += v - v0;
		sum2 += v * v - v0 * v0;
		return super.apply(v);
	}

	double update() {
		if (dataSize <= 1) return 0;
		double avg = sum / dataSize;
		return sqrt((sum2 - 2 * sum * avg + dataSize* avg * avg) / (dataSize - 1));
	}

}
