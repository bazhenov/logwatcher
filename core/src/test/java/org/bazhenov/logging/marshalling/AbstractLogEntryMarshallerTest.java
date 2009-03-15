package org.bazhenov.logging.marshalling;

import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;

public abstract class AbstractLogEntryMarshallerTest {

	@Test
	public void marshalling() throws MarshallerException {
		Marshaller marshaller = getMarshaller();

		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntry(now(), "group", "message", Severity.info, "3df", cause);
		String data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	protected abstract Marshaller getMarshaller();
}
