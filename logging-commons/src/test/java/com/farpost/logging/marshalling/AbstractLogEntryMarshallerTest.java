package com.farpost.logging.marshalling;

import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractLogEntryMarshallerTest {

	private Marshaller marshaller;

	@BeforeMethod
	protected void setUp() throws Exception {
		marshaller = getMarshaller();
	}

	@Test
	public void marshalling() throws MarshallerException {
		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, "2fe", "default", null,
			cause);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	@Test
	public void marshallingEntryWithAttributes() throws MarshallerException {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("foo", "bar");
		attributes.put("bar", "foo");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, null, "default", attributes, null);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	protected abstract Marshaller getMarshaller() throws Exception;
}
