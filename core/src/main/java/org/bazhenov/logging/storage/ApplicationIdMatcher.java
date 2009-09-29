package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;

public class ApplicationIdMatcher implements LogEntryMatcher {

	private final String applicationId;

	public ApplicationIdMatcher(String applicationId) {
		this.applicationId = applicationId;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return entry.getSampleEntry().getApplicationId().equals(applicationId);
	}

	public String getApplicationId() {
		return applicationId;
	}
}
