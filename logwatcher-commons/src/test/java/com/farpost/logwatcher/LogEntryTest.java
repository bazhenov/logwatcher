package com.farpost.logwatcher;

import org.testng.annotations.Test;

import java.util.Date;

import static com.farpost.logwatcher.Severity.info;
import static com.farpost.logwatcher.Severity.warning;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LogEntryTest {

	@Test
	public void entryParameters() {
		String logMessage = "ExceptionMessage";
		Date date = new Date(1226467380000L); // 2008, November 12, 15:23:00
		String group = "group";
		String checksum = "3d4f";
		LogEntry logEntry = new LogEntryImpl(date, group, logMessage, info, checksum, "default", null);

		assertThat(logEntry.getDate(), equalTo(date));
		assertThat(logEntry.getGroup(), equalTo(group));
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
		LogEntry entry = new LogEntryImpl(new Date(), "group", "message", warning, "3d", "default", null, cause);

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
