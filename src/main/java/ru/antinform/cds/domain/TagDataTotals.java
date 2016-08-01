package ru.antinform.cds.domain;

@SuppressWarnings("WeakerAccess")
public class TagDataTotals {

	public long count;
	public double valueSum;

	public void add(long count, double valueSum) {
		this.count += count;
		this.valueSum += valueSum;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public double getValueSum() {
		return valueSum;
	}

	public void setValueSum(double valueSum) {
		this.valueSum = valueSum;
	}

	@Override
	public String toString() {
		return "TagDataTotals{" +
			"count=" + count +
			", valueSum=" + valueSum +
			'}';
	}
}
