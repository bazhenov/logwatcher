package com.farpost.logwatcher;

import java.io.Serializable;
import java.util.Comparator;

public class ByTitleComparator implements Comparator<AggregatedEntry>, Serializable {

	public int compare(AggregatedEntry o1, AggregatedEntry o2) {
		return o1.getMessage().compareTo(o2.getMessage());
	}
}
