package com.farpost.logwatcher;

import com.farpost.logwatcher.statistics.ByDayStatistic;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import static java.lang.Math.signum;

public class ByLastOccurrenceDateComparator implements Comparator<Cluster>, Serializable {

	private final Map<Checksum, ByDayStatistic> dayStatisticMap;

	public ByLastOccurrenceDateComparator(Map<Checksum, ByDayStatistic> dayStatisticMap) {
		this.dayStatisticMap = dayStatisticMap;
	}

	public int compare(Cluster o1, Cluster o2) {
		return (int) signum(lastSeenAt(o2) - lastSeenAt(o1));
	}

	private long lastSeenAt(Cluster cluster) {
		return dayStatisticMap.containsKey(cluster.getChecksum())
			? dayStatisticMap.get(cluster.getChecksum()).getLastSeenAt().getMillis()
			: 0;
	}
}
