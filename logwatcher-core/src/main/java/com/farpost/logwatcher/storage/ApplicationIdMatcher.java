package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

public class ApplicationIdMatcher implements LogEntryMatcher {

	private final String applicationId;

	public ApplicationIdMatcher(String applicationId) {
		if ( applicationId == null ) {
			throw new NullPointerException("Application id must not be null");
		}
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

	@Override
	public String toString() {
		return "at:"+applicationId;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ApplicationIdMatcher that = (ApplicationIdMatcher) o;

		return applicationId.equals(that.applicationId);
	}

	@Override
	public int hashCode() {
		return applicationId.hashCode();
	}
}
