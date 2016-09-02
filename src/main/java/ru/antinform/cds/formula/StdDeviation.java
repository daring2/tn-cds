package ru.antinform.cds.formula;

import org.apache.commons.math3.stat.StatUtils;
import static java.lang.Math.sqrt;

public class StdDeviation extends WindowFunction {

	public StdDeviation(int size) {
		super(size);
	}

	double update() {
		return sqrt(StatUtils.variance(values, 0, dataSize));
	}

}
