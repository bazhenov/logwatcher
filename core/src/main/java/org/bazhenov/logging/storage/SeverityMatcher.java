package org.bazhenov.logging.storage;

import org.bazhenov.logging.Severity;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.w3c.dom.html.HTMLObjectElement;

public class SeverityMatcher implements LogEntryMatcher {

	private final Severity severity;

	public SeverityMatcher(Severity severity) {
		this.severity = severity;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		LogEntry sampleEntry = entry.getSampleEntry();
		return sampleEntry.getSeverity().isMoreImportantThan(severity);
	}

	public Severity getSeverity() {
		return severity;
	}
}
