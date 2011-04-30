package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;

public class CauseTypeMatcher implements LogEntryMatcher {

	private final String expectedType;

	public CauseTypeMatcher(String expectedType) {
		this.expectedType = expectedType;
	}

	public boolean isMatch(LogEntry entry) {
		Cause cause = entry.getCause();
		while (cause != null) {
			if (cause.getType().equals(expectedType)) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}

	public String getExpectedType() {
		return expectedType;
	}
}
