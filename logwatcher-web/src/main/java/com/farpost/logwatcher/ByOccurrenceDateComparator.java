package com.farpost.logwatcher;

import java.io.Serializable;
import java.util.Comparator;

public class ByOccurrenceDateComparator implements Comparator<LogEntry>, Serializable {

	public int compare(LogEntry o1, LogEntry o2) {
		return (int) (o1.getDate().getTime() - o2.getDate().getTime());
	}
}
