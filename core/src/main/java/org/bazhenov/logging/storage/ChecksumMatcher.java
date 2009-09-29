package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;

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

	public String getChecksum() {
		return checksum;
	}
}
