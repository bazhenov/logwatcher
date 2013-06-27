package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class InMemoryClusterStatistics implements ClusterStatistic {

	private final Set<String> activeApplications = newHashSet();
	private final Table<String, Checksum, StatTuple> statTable = HashBasedTable.create();
	private final Table<String, LocalDate, Set<Checksum>> clustersByTheDay = HashBasedTable.create();

	@Override
	public synchronized void registerEvent(String applicationId, DateTime dateTime, Checksum checksum) {
		activeApplications.add(applicationId.toLowerCase());

		if (statTable.contains(applicationId, checksum)) {
			statTable.get(applicationId, checksum).register(dateTime);
		} else {
			statTable.put(applicationId, checksum, new StatTuple(dateTime));
		}
		LocalDate date = new LocalDate(dateTime);
		Set<Checksum> clusterChecksums = clustersByTheDay.get(applicationId, date);
		if (clusterChecksums == null) {
			clusterChecksums = newHashSet();
			clustersByTheDay.put(applicationId, date, clusterChecksums);
		}
		clusterChecksums.add(checksum);
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

	@Override
	public Collection<Checksum> getActiveClusterChecksums(String applicationId, LocalDate date) {
		return clustersByTheDay.contains(applicationId, date)
			? clustersByTheDay.get(applicationId, date)
			: Collections.<Checksum>emptySet();
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
