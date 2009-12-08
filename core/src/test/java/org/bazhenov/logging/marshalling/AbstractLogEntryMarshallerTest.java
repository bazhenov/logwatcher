package org.bazhenov.logging.marshalling;

import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;

import java.util.*;

public abstract class AbstractLogEntryMarshallerTest {

	@Test
	public void marshalling() throws MarshallerException {
		Marshaller marshaller = getMarshaller();

		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntry(now(), "group", "message", Severity.info, "3df", "default", null,
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
		LogEntry entry = new LogEntry(now(), "group", "message", Severity.info, "3df", "default",
			attributes, null);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	protected abstract Marshaller getMarshaller();
}
