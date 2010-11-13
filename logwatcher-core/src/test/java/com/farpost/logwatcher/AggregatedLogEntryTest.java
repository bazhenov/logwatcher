package com.farpost.logwatcher;

import com.farpost.logwatcher.AggregatedAttribute;
import com.farpost.logwatcher.AggregatedLogEntryImpl;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

		Map<String, AggregatedAttribute> attributes = aggregated.getAttributes();
		assertThat(attributes.get("server").getCountFor("host1"), equalTo(2));
		assertThat(attributes.get("user").getCountFor("david"), equalTo(1));
		assertThat(attributes.get("user").getCountFor("john"), equalTo(1));
	}
}
