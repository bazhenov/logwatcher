package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.AggregatedLogEntry;

public class DateMatcher implements LogEntryMatcher {

	private final Date date;

	public DateMatcher(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return entry.getLastTime().getDate().equals(date);
	}
}
