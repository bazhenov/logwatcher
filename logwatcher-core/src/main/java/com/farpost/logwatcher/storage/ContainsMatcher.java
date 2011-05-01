package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;

public class ContainsMatcher implements LogEntryMatcher {

	private final String needle;

	public ContainsMatcher(String needle) {
		this.needle = needle;
	}

	public boolean isMatch(LogEntry entry) {
		return entry.getMessage().contains(needle) ||
			entry.getCause() != null && entry.getCause().getStackTrace().contains(needle);
	}

	public String getNeedle() {
		return needle;
	}
}
