package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DayStatistic {

	private final String applicationId;
	private final Checksum checksum;
	private final LocalDate date;

	@Nullable
	private final DateTime lastSeenAt;
	private final int count;

	public DayStatistic(String applicationId, Checksum checksum, LocalDate date, DateTime lastSeenAt, int count) {
		this.applicationId = checkNotNull(applicationId);
		this.checksum = checkNotNull(checksum);
		this.date = checkNotNull(date);
		this.lastSeenAt = lastSeenAt;
		this.count = count;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Checksum getChecksum() {
		return checksum;
	}

	public LocalDate getDate() {
		return date;
	}

	@Nullable
	public DateTime getLastSeenAt() {
		return lastSeenAt;
	}

	public int getCount() {
		return count;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DayStatistic)) return false;

		DayStatistic that = (DayStatistic) o;

		if (count != that.count) return false;
		if (!applicationId.equals(that.applicationId)) return false;
		if (!checksum.equals(that.checksum)) return false;
		if (!date.equals(that.date)) return false;
		return !(lastSeenAt != null ? !lastSeenAt.equals(that.lastSeenAt) : that.lastSeenAt != null);
	}

	@Override
	public int hashCode() {
		int result = applicationId.hashCode();
		result = 31 * result + checksum.hashCode();
		result = 31 * result + date.hashCode();
		result = 31 * result + (lastSeenAt != null ? lastSeenAt.hashCode() : 0);
		result = 31 * result + count;
		return result;
	}
}
