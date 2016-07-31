package ru.antinform.cds.utils;

import com.google.common.base.Stopwatch;
import java.util.concurrent.Callable;

public class ProfileUtils {

	public static <T> T logCall(Stopwatch sw, Callable<T> task) throws Exception {
		try {
			sw.reset(); sw.start();
			return task.call();
		} finally {
			sw.stop();
		}
	}

	public static void logVoidCall(Stopwatch sw, VoidCallable task) throws Exception {
		logCall(sw, () -> { task.call(); return null; });
	}

	private ProfileUtils() {
	}
}
