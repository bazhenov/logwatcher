package com.farpost.logging.marshalling;

import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractLogEntryMarshallerTest {

	@Test
	public void marshalling() throws MarshallerException {
		Marshaller marshaller = getMarshaller();

		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntry(now(), "group", "message", Severity.info, "2fe", "default", null,
			cause);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	@Test
	public void marshallingEntryWithAttributes() throws MarshallerException {
		Marshaller marshaller = getMarshaller();

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("foo", "bar");
		attributes.put("bar", "foo");
		LogEntry entry = new LogEntry(now(), "group", "message", Severity.info, null, "default",
			attributes, null);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	protected abstract Marshaller getMarshaller();
}
