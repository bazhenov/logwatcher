package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class InMemoryClusterStatistics implements ClusterStatistic {

	private final Set<String> activeApplications = newHashSet();
	private final Table<String, Checksum, StatTuple> statTable = HashBasedTable.create();
	private final Table<String, Checksum, MinuteVector> minuteVectorTable = HashBasedTable.create();
	private final Table<String, LocalDate, Set<Checksum>> clustersByTheDay = HashBasedTable.create();

	@Override
	public synchronized void registerEvent(String applicationId, DateTime dateTime, Checksum checksum) {
		activeApplications.add(applicationId.toLowerCase());

		if (statTable.contains(applicationId, checksum)) {
			statTable.get(applicationId, checksum).register(dateTime);
		} else {
			statTable.put(applicationId, checksum, new StatTuple(dateTime));
		}

		if (minuteVectorTable.contains(applicationId, checksum)) {
			minuteVectorTable.get(applicationId, checksum).increment(dateTime);
		} else {
			MinuteVector v = new MinuteVector();
			v.increment(dateTime);
			minuteVectorTable.put(applicationId, checksum, v);
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
	public MinuteVector getMinuteVector(String applicationId, Checksum checksum) {
		return firstNonNull(minuteVectorTable.get(applicationId, checksum), new MinuteVector());
	}

	@Override
	public Set<String> getActiveApplications() {
		return activeApplications;
	}

	@Override
	public ByDayStatistic getByDayStatistic(String applicationId, Checksum checksum) {
		StatTuple tuple = statTable.get(applicationId, checksum);
		return tuple != null
			? new ByDayStatistic(applicationId, checksum, tuple.lastSeen, tuple.counts)
			: null;
	}

	@Override
	public Collection<Checksum> getActiveClusterChecksums(String applicationId, LocalDate date) {
		return clustersByTheDay.contains(applicationId, date)
			? clustersByTheDay.get(applicationId, date)
			: Collections.<Checksum>emptySet();
	}

	private static class StatTuple {

		Map<LocalDate, Integer> counts = newHashMap();
		DateTime lastSeen;

		private StatTuple(DateTime lastSeen) {
			counts.put(new LocalDate(lastSeen), 1);
			this.lastSeen = lastSeen;
		}

		public void register(DateTime date) {
			LocalDate localDate = new LocalDate(date);
			int prevCount = counts.containsKey(localDate) ? counts.get(localDate) : 0;
			counts.put(localDate, prevCount + 1);
			if (date.isAfter(lastSeen))
				lastSeen = date;
		}
	}
}
