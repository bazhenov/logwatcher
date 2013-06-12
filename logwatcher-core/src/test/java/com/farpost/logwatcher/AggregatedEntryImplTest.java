package com.farpost.logwatcher;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AggregatedEntryImplTest {

	@Test
	public void jsonMappingShouldBePossible() throws IOException {
		AggregatedEntry entry = new AggregatedEntryImpl("foo", "vasv", "sadfs", Severity.debug, 32, new DateTime(), null);

		ObjectMapper mapper = new ObjectMapper();
		String s = mapper.writeValueAsString(entry);
		AggregatedEntry entryCopy = mapper.readValue(s, AggregatedEntryImpl.class);
		assertThat(entryCopy, equalTo(entry));
	}
}
