package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class InMemoryClusterStatistics implements ClusterStatistic {

	Set<String> activeApplications = newHashSet();
	Table<String, Checksum, StatTuple> statTable = HashBasedTable.create();

	@Override
	public void registerEvent(String applicationId, DateTime date, Checksum checksum) {
		activeApplications.add(applicationId.toLowerCase());

		if (statTable.contains(applicationId, checksum)) {
			statTable.get(applicationId, checksum).register(date);
		} else {
			statTable.put(applicationId, checksum, new StatTuple(date));
		}
	}

	@Override
	public Set<String> getActiveApplications() {
		return activeApplications;
	}

	@Override
	public DayStatistic getDayStatistic(String applicationId, Checksum checksum, LocalDate date) {
		StatTuple tuple = statTable.get(applicationId, checksum);
		return tuple != null
			? new DayStatistic(applicationId, checksum, date, tuple.lastSeen, tuple.count)
			: new DayStatistic(applicationId, checksum, date, null, 0);
	}

	private static class StatTuple {

		int count;
		DateTime lastSeen;

		private StatTuple(DateTime lastSeen) {
			this.count = 1;
			this.lastSeen = lastSeen;
		}

		public void register(DateTime date) {
			count++;
			if (date.isAfter(lastSeen))
				lastSeen = date;
		}
	}
}
