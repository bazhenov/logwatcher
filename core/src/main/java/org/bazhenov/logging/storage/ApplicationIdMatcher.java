package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

public class ApplicationIdMatcher implements LogEntryMatcher {

	private final String applicationId;

	public ApplicationIdMatcher(String applicationId) {
		this.applicationId = applicationId;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return entry.getSampleEntry().getApplicationId().equals(applicationId);
	}

	public boolean isMatch(LogEntry entry) {
		return applicationId.equals(entry.getApplicationId());
	}

	public String getApplicationId() {
		return applicationId;
	}
}
