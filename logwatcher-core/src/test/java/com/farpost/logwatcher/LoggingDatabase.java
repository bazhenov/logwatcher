package com.farpost.logwatcher;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;

import static com.farpost.timepoint.DateTime.now;

public class LoggingDatabase {

	public LogEntry createLogEntry() {
		return new LogEntryImpl(now(), "group", "message", Severity.info, "3d4f", "default", null);
	}
}
