package org.bazhenov.logging;

import java.util.Comparator;

public class ByLastOccurenceDateComparator implements Comparator<AggregatedLogEntry> {

	public int compare(AggregatedLogEntry o1, AggregatedLogEntry o2) {
		return (int) (o2.getLastTime().asTimestamp() - o1.getLastTime().asTimestamp());
	}
}
