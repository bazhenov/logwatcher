package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;

import java.util.Comparator;

import com.farpost.timepoint.DateComparator;

public class AggregatedLogEntryComparator implements Comparator<AggregatedLogEntry> {

	private final DateComparator dateComparator = new DateComparator();

	public int compare(AggregatedLogEntry o1, AggregatedLogEntry o2) {
		return -dateComparator.compare(o1.getLastTime(), o2.getLastTime());
	}
}
