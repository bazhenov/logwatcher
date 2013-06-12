package com.farpost.logwatcher;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

import java.util.concurrent.atomic.AtomicInteger;

public class AggregatedEntryImpl implements AggregatedEntry {

	private volatile DateTime lastTime;
	private final AtomicInteger count;
	private final String message;
	private final Cause sampleCause;
	private final String checksum;
	private final Severity severity;
	private final String applicationId;

	@JsonCreator
	public AggregatedEntryImpl(@JsonProperty("message") String message, @JsonProperty("checksum") String checksum,
														 @JsonProperty("applicationId") String applicationId,
														 @JsonProperty("severity") Severity severity, @JsonProperty("count") int count,
														 @JsonProperty("lastTime") ReadableDateTime lastTime) {
		this(message, checksum, applicationId, severity, count, lastTime, null);
	}

	public AggregatedEntryImpl(String message, String checksum, String applicationId, Severity severity, int count,
														 ReadableDateTime lastTime, Cause sampleCause) {
		this.applicationId = applicationId;
		this.severity = severity;
		this.lastTime = lastTime.toDateTime();
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

	@JsonIgnore
	public Cause getSampleCause() {
		return sampleCause;
	}

	public String getChecksum() {
		return checksum;
	}

	public void happensAgain(int times, ReadableDateTime lastTime) {
		if (lastTime.isAfter(this.lastTime)) {
			this.lastTime = lastTime.toDateTime();
		}
		count.addAndGet(times);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AggregatedEntryImpl)) return false;

		AggregatedEntryImpl that = (AggregatedEntryImpl) o;

		if (!applicationId.equals(that.applicationId)) return false;
		if (!checksum.equals(that.checksum)) return false;
		if (count.get() != that.count.get()) return false;
		if (lastTime.compareTo(that.lastTime) != 0) return false;
		if (!message.equals(that.message)) return false;
		if (sampleCause != null ? !sampleCause.equals(that.sampleCause) : that.sampleCause != null) return false;
		if (severity != that.severity) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = lastTime.hashCode();
		result = 31 * result + count.hashCode();
		result = 31 * result + message.hashCode();
		result = 31 * result + (sampleCause != null ? sampleCause.hashCode() : 0);
		result = 31 * result + checksum.hashCode();
		result = 31 * result + severity.hashCode();
		result = 31 * result + applicationId.hashCode();
		return result;
	}
}
