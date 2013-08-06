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
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.UnknownHostException;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static java.net.InetAddress.getLocalHost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.joda.time.DateTime.now;

public class WriteToStorageTransportListenerTest {

	private final Marshaller marshaller = new Jaxb2Marshaller();
	private LogStorage storage;
	private TransportListener listener;

	@BeforeMethod
	protected void setUp() throws Exception {
		storage = new InMemoryLogStorage();
		listener = new WriteToStorageTransportListener(storage, marshaller);
	}

	@Test
	public void listenerShouldWriteEntryToDatabase()
		throws TransportException, LogStorageException, InvalidCriteriaException, UnknownHostException {
		DateTime date = now();
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntryImpl(date.toDate(), "group", "message", Severity.error, "checksum", "default", null,
			cause);

		listener.onMessage(marshaller.marshall(entry), getLocalHost());

		int count = entries().
			date(date.toLocalDate()).
			count(storage);
		assertThat(count, equalTo(1));
	}
}