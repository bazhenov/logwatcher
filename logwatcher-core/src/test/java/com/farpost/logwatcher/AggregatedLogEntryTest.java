package com.farpost.logwatcher;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;

public class AggregatedLogEntryTest {

	@Test
	public void clientCanAggregateSeveralEntries() {
		Map<String, String> firstEntryAttributes = new HashMap<String, String>() {{
			put("server", "host1");
			put("user", "john");
		}};
		LogEntry firstEntry = new LogEntryImpl(now(), "group", "message", Severity.debug, "af1", "frontend",
			firstEntryAttributes, null);

		Map<String, String> secondEntryAttributes = new HashMap<String, String>() {{
			put("server", "host1");
			put("user", "david");
		}};
		LogEntry secondEntry = new LogEntryImpl(now(), "group", "message", Severity.debug, "af1", "frontend",
			secondEntryAttributes, null);

		AggregatedLogEntryImpl aggregated = new AggregatedLogEntryImpl(firstEntry);
		aggregated.merge(new AggregatedLogEntryImpl(secondEntry));
	}
}
