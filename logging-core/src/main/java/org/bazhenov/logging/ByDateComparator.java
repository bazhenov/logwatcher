package org.bazhenov.logging;

import java.util.*;

public class ByDateComparator implements Comparator<LogEntry> {

	public int compare(LogEntry o1, LogEntry o2) {
		return (int) (o2.getDate().asTimestamp() - o1.getDate().asTimestamp());
	}
}