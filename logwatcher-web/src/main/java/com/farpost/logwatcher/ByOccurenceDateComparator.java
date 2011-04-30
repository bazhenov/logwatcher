package com.farpost.logwatcher;

import java.io.Serializable;
import java.util.Comparator;

public class ByOccurenceDateComparator implements Comparator<LogEntry>, Serializable {

	public int compare(LogEntry o1, LogEntry o2) {
		return (int) (o2.getDate().asTimestamp() - o1.getDate().asTimestamp());
	}
}
