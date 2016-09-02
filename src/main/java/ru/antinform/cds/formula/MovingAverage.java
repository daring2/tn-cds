package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.StatUtils;
import static java.lang.Integer.min;

@SuppressWarnings("WeakerAccess")
public class MovingAverage implements Formula {

	final int size;
	final double[] values;

	int index;

	public MovingAverage(int size) {
		this.size = size;
		values = new double[size];
	}

	@Override
	public double apply(double value) {
		values[index++ % size] = value;
		return StatUtils.mean(values, 0, min(index, size));
	}

}
