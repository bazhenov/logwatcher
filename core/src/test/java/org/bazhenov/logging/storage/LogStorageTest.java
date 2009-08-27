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
		DateTime now = now();

		LogEntry entry = newEntry().create();
		storage.writeEntry(entry);

		assertThat(storage.getEntryCount(now.getDate()), equalTo(1));
		assertThat(storage.getEntryCount(now.plusDay(1).getDate()), equalTo(0));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
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
		LogEntry entry = newEntry().create();

		storage.writeEntry(entry);

		int count = from(storage).count();
		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanCountEntriesByCriteria() throws LogStorageException {
		DateTime now = now();
		DateTime yesterday = now.minusDay(1);

		newEntry().
			occuredAt(yesterday).
			checksum("2fe").
			saveIn(storage);
		newEntry().
			occuredAt(now).
			checksum("3fe").
			saveIn(storage);

		int count = from(storage).
			date(now.getDate()).
			count();
		assertThat(count, equalTo(1));

		count = from(storage).
			date(yesterday.getDate()).
			count();
		assertThat(count, equalTo(1));

		count = from(storage).
			date(yesterday.minusDay(1).getDate()).
			count();
		assertThat(count, equalTo(0));
	}

	private LogEntryBuilder newEntry() {
		return new LogEntryBuilder();
	}

	protected abstract LogStorage createStorage() throws Exception;
}
