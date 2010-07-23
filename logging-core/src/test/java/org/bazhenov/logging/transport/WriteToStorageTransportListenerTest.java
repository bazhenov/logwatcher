package org.bazhenov.logging.transport;

import com.farpost.logging.marshalling.JDomMarshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;
import org.bazhenov.logging.storage.InMemoryLogStorage;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.testng.annotations.Test;

import static com.farpost.timepoint.DateTime.now;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
