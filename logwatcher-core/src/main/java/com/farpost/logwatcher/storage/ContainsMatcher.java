package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedLogEntry;
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
