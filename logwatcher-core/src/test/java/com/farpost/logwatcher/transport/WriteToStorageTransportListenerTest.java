package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.InMemoryLogStorage;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.timepoint.DateTime;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WriteToStorageTransportListenerTest {

	@Test
	public void listenerShouldWriteEntryToDatabase()
		throws TransportException, LogStorageException, InvalidCriteriaException {
		DateTime date = now();
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntryImpl(date, "group", "message", Severity.error, "checksum", "default",
			null, cause);

		LogStorage storage = new InMemoryLogStorage();
		Marshaller marshaller = new Jaxb2Marshaller();
		TransportListener listener = new WriteToStorageTransportListener(storage, marshaller);
		listener.onMessage(marshaller.marshall(entry));

		int count = entries().
			date(date.getDate()).
			count(storage);
		assertThat(count, equalTo(1));
	}
}
