package com.farpost.logwatcher;

/**
 * Класс занимающийся вытаскиванием
 * stacktrace'a из исключений.
 * <p/>
 * Метод вытаскивания отличается от оригинального {@link Throwable#printStackTrace()}
 * отсутсвием в результирующей строке неполного stacktrace'a caused-исключения
 */
public class StackTraceFormatter {

	public static String extractStackTrace(Throwable throwable) {
		StringBuilder stackTraceBuilder = new StringBuilder();
		StackTraceElement[] trace = throwable.getStackTrace();
		stackTraceBuilder.append(throwable);
		for (StackTraceElement traceElement : trace) {
			stackTraceBuilder.append("\n\tat ").append(traceElement);
		}

		return stackTraceBuilder.append("\n").toString();
	}
}
