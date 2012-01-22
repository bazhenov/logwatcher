package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.joda.time.DateTime.now;

public abstract class AbstractLogEntryMarshallerTest {

	protected Marshaller marshaller;

	@BeforeMethod
	protected void setUp() throws Exception {
		marshaller = getMarshaller();
	}

	@Test
	public void marshalling() {
		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, "2fe", "default", null,
			cause);
		byte[] data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy.getDate(), equalTo(entry.getDate()));
		assertThat(entryCopy.getSeverity(), equalTo(entry.getSeverity()));
		assertThat(entryCopy.getApplicationId(), equalTo(entry.getApplicationId()));
		assertThat(entryCopy.getCategory(), equalTo(entry.getCategory()));
		assertThat(entryCopy.getChecksum(), equalTo(entry.getChecksum()));
		assertThat(entryCopy.getMessage(), equalTo(entry.getMessage()));
		assertThat(entryCopy.getAttributes(), equalTo(entry.getAttributes()));
		assertThat(entryCopy.getCause(), equalTo(entry.getCause()));
	}

	@Test
	public void marshallingEntryWithAttributes() {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("foo", "bar");
		attributes.put("bar", "foo");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, "1fe", "default", attributes, null);
		byte[] data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	protected abstract Marshaller getMarshaller() throws Exception;
}
