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
		this.title = entry.getMessage();
		this.text = formatCause(cause);
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

	private static String formatCause(Cause rootCause) {
		StringBuilder prefix = new StringBuilder();
		StringBuilder stackTrace = new StringBuilder();

		if ( rootCause != null ) {
			Cause cause = rootCause;
			while ( cause != null ) {
				if ( cause != rootCause ) {
					stackTrace.append("\n\n").append(prefix).append("Caused by ");
				}
				String iStack = cause.getStackTrace().replaceAll("\n", "\n" + prefix);
				stackTrace.append(cause.getType())
					.append(": ")
					.append(cause.getMessage())
					.append("\n")
					.append(prefix)
					.append(iStack);
				cause = cause.getCause();
				prefix.append("  ");
			}
		}
		return stackTrace.toString();
	}
}
