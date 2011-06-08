package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.Severity.info;
import static com.farpost.logwatcher.Severity.warning;
import static com.farpost.timepoint.Date.november;
import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LogEntryTest {

	@Test
	public void entryParameters() {
		String logMessage = "ExceptionMessage";
		DateTime date = november(12, 2008).at("15:23");
		String group = "group";
		String checksum = "3d4f";
		LogEntry logEntry = new LogEntryImpl(date, group, logMessage, info, checksum, "default", null);

		assertThat(logEntry.getDate(), equalTo(date));
		assertThat(logEntry.getCategory(), equalTo(group));
		assertThat(logEntry.getMessage(), equalTo(logMessage));
		assertThat(logEntry.getSeverity(), equalTo(info));
		assertThat(logEntry.getChecksum(), equalTo(checksum));
	}

	@Test
	public void cause() {
		String stackTrace = "stackTrace";
		String message = "message";
		String type = "type";
		Cause cause = new Cause(type, message, stackTrace);

		assertThat(cause.getType(), equalTo(type));
		assertThat(cause.getMessage(), equalTo(message));
		assertThat(cause.getStackTrace(), equalTo(stackTrace));
	}

	@Test
	public void entryMayHaveCause() {
		Cause cause = createCause();
		LogEntry entry = new LogEntryImpl(now(), "group", "message", warning, "3d", "default", null, cause);

		assertThat(entry.getCause(), equalTo(cause));
	}

	@Test
	public void causeMayHaveNestedCause() {
		Cause nestedCause = createCause();
		Cause cause = new Cause("type", "message", "stackTrace", nestedCause);

		assertThat(cause.getCause(), equalTo(nestedCause));
	}

	private Cause createCause() {
		return new Cause("type", "message", "stackTrace");
	}
}
