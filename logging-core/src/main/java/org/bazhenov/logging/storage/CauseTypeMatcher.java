package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;

public class CauseTypeMatcher implements LogEntryMatcher {

	private final String expectedType;

	public CauseTypeMatcher(String expectedType) {
		this.expectedType = expectedType;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		throw new UnsupportedOperationException();
	}

	public boolean isMatch(LogEntry entry) {
		Cause cause = entry.getCause();
		while ( cause != null ) {
			if ( cause.getType().equals(expectedType) ) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}
}
