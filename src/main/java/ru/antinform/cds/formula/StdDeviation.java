package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.StatUtils;
import static java.lang.Integer.min;
import static java.lang.Math.sqrt;

@SuppressWarnings("WeakerAccess")
public class StdDeviation implements Formula {

	final int size;
	final double[] values;

	int index;

	public StdDeviation(int size) {
		this.size = size;
		values = new double[size];
	}

	@Override
	public double apply(double value) {
		values[index++ % size] = value;
		return sqrt(StatUtils.variance(values, 0, min(index, size)));
	}

}
