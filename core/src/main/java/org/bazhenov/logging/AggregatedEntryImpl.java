package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

public class AggregatedEntryImpl implements AggregatedEntry {

	private final DateTime lastTime;
	private final int count;
	private final String message;
	private final Cause sampleCause;
	private final String checksum;
	private final Severity severity;

	public AggregatedEntryImpl(String message, String checksum, Severity severity, int count,
	                           DateTime lastTime, Cause sampleCause) {
		this.severity = severity;
		this.lastTime = lastTime;
		this.checksum = checksum;
		this.count = count;
		this.message = message;
		this.sampleCause = sampleCause;
	}

	public DateTime getLastTime() {
		return lastTime;
	}

	public Severity getSeverity() {
		return severity;
	}

	public int getCount() {
		return count;
	}

	public String getMessage() {
		return message;
	}

	public Cause getSampleCause() {
		return sampleCause;
	}

	public String getChecksum() {
		return checksum;
	}
}
