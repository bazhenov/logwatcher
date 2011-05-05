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

	@Override
	public String toString() {
		return "contains: " + needle;
	}

	@Override
	public int hashCode() {
		return needle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		ContainsMatcher that = (ContainsMatcher) obj;
		return needle.equals(that.getNeedle());
	}
}
