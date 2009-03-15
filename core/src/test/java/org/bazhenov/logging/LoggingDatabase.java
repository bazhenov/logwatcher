package org.bazhenov.logging;

import static com.farpost.timepoint.DateTime.now;

public class LoggingDatabase {

	public LogEntry createLogEntry() {
		return new LogEntry(now(), "group", "message", Severity.info, "3d4f");
	}
}
