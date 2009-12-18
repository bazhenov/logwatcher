package org.bazhenov.logging.transport;

import com.farpost.timepoint.DateTime;
import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import org.bazhenov.logging.marshalling.*;
import org.bazhenov.logging.storage.*;

import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;

public class WriteToStorageTransportListenerTest {

	@Test
	public void listenerShouldWriteEntryToDatabase()
		throws TransportException, MarshallerException, LogStorageException, InvalidCriteriaException {
		DateTime date = now();
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntry(date, "group", "message", Severity.error, "checksum", "default",
			null, cause);

		LogStorage storage = new InMemoryLogStorage();
		Marshaller marshaller = new JDomMarshaller();
		TransportListener listener = new WriteToStorageTransportListener(storage, marshaller);
		listener.onMessage(marshaller.marshall(entry));

		int count = entries().
			date(date.getDate()).
			count(storage);
		assertThat(count, equalTo(1));
	}
}
