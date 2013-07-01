package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ByDayStatistic {

	private final String applicationId;
	private final Checksum checksum;
	private final DateTime lastSeenAt;
	private final Map<LocalDate, Integer> count;

	public ByDayStatistic(String applicationId, Checksum checksum, DateTime lastSeen, Map<LocalDate, Integer> counts) {
		this.applicationId = checkNotNull(applicationId);
		this.checksum = checkNotNull(checksum);
		this.lastSeenAt = checkNotNull(lastSeen);
		this.count = checkNotNull(counts);
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Checksum getChecksum() {
		return checksum;
	}

	public DateTime getLastSeenAt() {
		return lastSeenAt;
	}

	public int getTodayCount() {
		return getCount(LocalDate.now());
	}

	public int getYesterdayCount() {
		return getCount(LocalDate.now().minusDays(1));
	}

	public int getCount(LocalDate date) {
		return count.containsKey(date) ? count.get(date) : 0;
	}
}
