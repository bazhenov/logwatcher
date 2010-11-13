package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;

public class SeverityMatcher implements LogEntryMatcher {

	private final Severity severity;

	public SeverityMatcher(Severity severity) {
		if ( severity == null ) {
			throw new NullPointerException("Severity must not be null");
		}
		this.severity = severity;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		LogEntry sampleEntry = entry.getSampleEntry();
		return sampleEntry.getSeverity().isMoreImportantThan(severity);
	}

	public boolean isMatch(LogEntry entry) {
		return entry.getSeverity().isMoreImportantThan(severity);
	}

	public Severity getSeverity() {
		return severity;
	}

	@Override
	public String toString() {
		return "severity:" + severity;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SeverityMatcher that = (SeverityMatcher) o;
		return severity == that.severity;
	}

	@Override
	public int hashCode() {
		return severity.hashCode();
	}
}
