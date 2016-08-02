package ru.antinform.cds.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;

public class ConcurrentUtils {

	public static ThreadFactory newThreadFactory(String nameFormat, boolean daemon) {
		return new ThreadFactoryBuilder().
			setNameFormat(nameFormat).
			setDaemon(daemon).
			build();
	}

	private ConcurrentUtils() {
	}
}
