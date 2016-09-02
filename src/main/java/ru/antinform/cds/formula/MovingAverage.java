package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.StatUtils;

public class MovingAverage extends WindowFunction {

	public MovingAverage(int size) {
		super(size);
	}

	double update() {
		return StatUtils.mean(values, 0, dataSize);
	}

}
