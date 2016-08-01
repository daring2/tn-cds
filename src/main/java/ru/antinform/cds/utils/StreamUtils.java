package ru.antinform.cds.utils;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}

	private StreamUtils() {
	}
}
