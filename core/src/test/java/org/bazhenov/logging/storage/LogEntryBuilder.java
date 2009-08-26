package org.bazhenov.logging.storage;

import org.bazhenov.logging.*;
import com.farpost.timepoint.DateTime;
import static com.farpost.timepoint.DateTime.now;

public class LogEntryBuilder {

	private DateTime time = now();
	private String group = "group";
	private String message = "message";
	private Severity severity = Severity.error;
	private String checksum = "2fde43";
	private Cause cause;
	private String applicationId = "some-application";

	public LogEntry create() {
		return new LogEntry(time, group, message, severity, checksum, cause, applicationId);
	}

	public LogEntryBuilder occuredAt(DateTime time) {
		this.time = time;
		return this;
	}
}
