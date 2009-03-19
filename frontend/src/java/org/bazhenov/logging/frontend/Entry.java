package org.bazhenov.logging.frontend;

import org.bazhenov.logging.*;
import com.farpost.timepoint.DateTime;

public class Entry {

	private final String title;
	private final String text;
	private final int count;
	private final DateTime lastTime;

	public Entry(AggregatedLogEntry aggregatedEntry) {
		LogEntry entry = aggregatedEntry.getSampleEntry();
		Cause cause = entry.getCause();
		String text = cause != null
			? cause.getStackTrace()
			: null;

		this.title = entry.getMessage();
		this.text = text;
		this.count = aggregatedEntry.getCount();
		this.lastTime = aggregatedEntry.getLastTime();
	}

	public Entry(String title, String text, int count, DateTime lastTime) {
		this.title = title;
		this.text = text;
		this.count = count;
		this.lastTime = lastTime;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public int getCount() {
		return count;
	}

	public DateTime getLastTime() {
		return lastTime;
	}
}
