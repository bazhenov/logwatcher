package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

public class ContainsMatcher implements LogEntryMatcher {

	private final String needle;

	public ContainsMatcher(String needle) {
		this.needle = needle;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return false;
	}

	public boolean isMatch(LogEntry entry) {
		return entry.getMessage().contains(needle);
	}
}
