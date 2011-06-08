package com.farpost.logwatcher.storage;

import java.util.Collection;

/**
 * Генерируется имплементациями {@link LogStorage} в случае, если они не могу обработать
 * переданные клиентом критерии
 */
public class InvalidCriteriaException extends RuntimeException {

	public InvalidCriteriaException(String message) {
		super(message);
	}

	public InvalidCriteriaException(Collection<LogEntryMatcher> badMatchers) {
		this("Some matchers are not physically linked to storage: [" + buildMatcersList(badMatchers) + "]");
	}

	public InvalidCriteriaException(Throwable cause) {
		super(cause);
	}

	private static String buildMatcersList(Collection<LogEntryMatcher> badMatchers) {
		StringBuilder builder = new StringBuilder();
		for (LogEntryMatcher matcher : badMatchers) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(matcher.getClass().getSimpleName());
		}
		return builder.toString();
	}
}
