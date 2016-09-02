package ru.antinform.cds.formula;

abstract class WindowFunction implements Formula {

	final int size;
	final double[] values;

	int index;
	int dataSize;

	WindowFunction(int size) {
		this.size = size;
		values = new double[size];
	}

	@Override
	public double apply(double value) {
		values[index] = value;
		index = index < size - 1 ? index + 1 : 0;
		if (dataSize < size) dataSize++;
		return update();
	}

	abstract double update();
}
