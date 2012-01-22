package com.farpost.logwatcher;

import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicInteger;

public class AggregatedEntryImpl implements AggregatedEntry {

	private volatile DateTime lastTime;
	private final AtomicInteger count;
	private final String message;
	private final Cause sampleCause;
	private final String checksum;
	private final Severity severity;
	private final String applicationId;

	public AggregatedEntryImpl(String message, String checksum, String applicationId,
														 Severity severity, int count, DateTime lastTime, Cause sampleCause) {
		this.applicationId = applicationId;
		this.severity = severity;
		this.lastTime = lastTime;
		this.checksum = checksum;
		this.count = new AtomicInteger(count);
		this.message = message;
		this.sampleCause = sampleCause;
	}

	public AggregatedEntryImpl(LogEntry entry) {
		this(entry.getMessage(), entry.getChecksum(), entry.getApplicationId(), entry.getSeverity(), 1,
			entry.getDate(), entry.getCause());
	}

	public DateTime getLastTime() {
		return lastTime;
	}

	public Severity getSeverity() {
		return severity;
	}

	public int getCount() {
		return count.intValue();
	}

	public String getMessage() {
		return message;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Cause getSampleCause() {
		return sampleCause;
	}

	public String getChecksum() {
		return checksum;
	}

	public void happensAgain(int times, DateTime lastTime) {
		if (lastTime.isAfter(this.lastTime)) {
			this.lastTime = lastTime;
		}
		count.addAndGet(times);
	}
}
