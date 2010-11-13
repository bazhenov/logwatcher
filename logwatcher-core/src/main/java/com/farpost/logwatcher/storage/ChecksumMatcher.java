package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedLogEntry;
import com.farpost.logwatcher.LogEntry;

public class ChecksumMatcher implements LogEntryMatcher {

	private final String checksum;

	public ChecksumMatcher(String checksum) {
		if ( checksum == null ) {
			throw new NullPointerException("No checksum given");
		}
		this.checksum = checksum;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return checksum.equals(entry.getSampleEntry().getChecksum());
	}

	public boolean isMatch(LogEntry entry) {
		return checksum.equals(entry.getChecksum());
	}

	public String getChecksum() {
		return checksum;
	}
}
