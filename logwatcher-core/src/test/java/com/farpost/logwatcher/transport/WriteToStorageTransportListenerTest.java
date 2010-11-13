package com.farpost.logwatcher.transport;

import com.farpost.logging.marshalling.Jaxb2Marshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import com.farpost.logwatcher.storage.InMemoryLogStorage;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.TransportListener;
import com.farpost.logwatcher.transport.WriteToStorageTransportListener;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WriteToStorageTransportListenerTest {

	@Test
	public void listenerShouldWriteEntryToDatabase()
		throws TransportException, MarshallerException, LogStorageException, InvalidCriteriaException {
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
