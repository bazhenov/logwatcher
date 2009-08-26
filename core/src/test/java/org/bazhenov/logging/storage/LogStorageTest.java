package org.bazhenov.logging.storage;

import static com.farpost.timepoint.Date.november;
import com.farpost.timepoint.DateTime;
import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.storage.LogEntries.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.*;

abstract public class LogStorageTest {

	private LogStorage storage;

	@BeforeMethod
	public void setUp() throws Exception {
		storage = createStorage();
	}

	@Test
	public void storageCanSaveEntry() throws Exception {
		DateTime date = now();

		LogEntry entry = newEntry().create();
		storage.writeEntry(entry);

		assertThat(storage.getEntryCount(date.getDate()), equalTo(1));
		assertThat(storage.getEntryCount(date.plusDay(1).getDate()), equalTo(0));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		LogStorage storage = createStorage();
		DateTime morning = november(12, 2008).at(11, 00);
		DateTime evening = november(12, 2008).at(18, 05);

		LogEntry entry1 = newEntry().
			occuredAt(morning).
			create();
		LogEntry entry2 = newEntry().
			occuredAt(evening).
			create();

		storage.writeEntry(entry1);
		storage.writeEntry(entry2);

		List<AggregatedLogEntry> list = storage.getEntries(morning.getDate());
		assertThat(list.size(), equalTo(1));
	}

	@Test
	public void storageCanCountEntries() throws Exception {
		LogStorage storage = createStorage();
		LogEntry entry = newEntry().create();

		storage.writeEntry(entry);

		int count = from(storage).count();
		assertThat(count, equalTo(1));
	}

	private LogEntryBuilder newEntry() {
		return new LogEntryBuilder();
	}

	protected abstract LogStorage createStorage() throws Exception;
}
