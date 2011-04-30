package com.farpost.logwatcher;

import java.io.Serializable;
import java.util.Comparator;

public class ByLastOccurenceDateComparator implements Comparator<AggregatedEntry>, Serializable {

	public int compare(AggregatedEntry o1, AggregatedEntry o2) {
		return (int) (o2.getLastTime().asTimestamp() - o1.getLastTime().asTimestamp());
	}
}
