package ru.antinform.cds.formula;

public class MovingAverage extends WindowFunction {

	private double sum;

	public MovingAverage(int size) {
		super(size);
	}

	public double apply(double value) {
		sum += value - values[index];
		return super.apply(value);
	}

	double update() {
		return sum / dataSize;
	}

}
