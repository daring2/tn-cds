package ru.antinform.cds.formula;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public interface Formula {

	public double apply(double value);

}
