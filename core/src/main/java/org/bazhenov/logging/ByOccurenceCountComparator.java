package org.bazhenov.logging;

import java.util.Comparator;

public class ByOccurenceCountComparator implements Comparator<AggregatedEntry> {

	public int compare(AggregatedEntry o1, AggregatedEntry o2) {
		return o2.getCount() - o1.getCount();
	}
}
