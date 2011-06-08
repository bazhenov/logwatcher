package com.farpost.logwatcher;

import static com.farpost.timepoint.DateTime.now;

public class LoggingDatabase {

	public LogEntry createLogEntry() {
		return new LogEntryImpl(now(), "group", "message", Severity.info, "3d4f", "default", null);
	}
}
