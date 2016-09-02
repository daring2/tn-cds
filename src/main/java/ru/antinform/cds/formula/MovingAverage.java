package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.StatUtils;

@SuppressWarnings("WeakerAccess")
public class MovingAverage extends WindowFunction {

	public MovingAverage(int size) {
		super(size);
	}

	@Override
	double update() {
		return StatUtils.mean(values, 0, dataSize);
	}

}
