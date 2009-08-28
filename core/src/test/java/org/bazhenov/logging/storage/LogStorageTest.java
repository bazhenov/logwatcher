package org.bazhenov.logging.storage;

import static com.farpost.timepoint.Date.november;
import static com.farpost.timepoint.Date.yesterday;
import static com.farpost.timepoint.Date.today;
import com.farpost.timepoint.DateTime;
import com.farpost.timepoint.Date;
import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.TestSupport.entry;
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
		Date today = today();

		LogEntry entry = entry().
			occured(today.at("11:35")).
			create();
		storage.writeEntry(entry);

		AggregatedLogEntry aggreagatedEntry = from(storage).
			date(today).
			findFirst();

		assertThat(aggreagatedEntry.getSampleEntry(), equalTo(entry));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		DateTime morning = november(12, 2008).at("11:00");
		DateTime evening = november(12, 2008).at("18:05");

		entry().
			occured(morning).
			saveIn(storage);
		entry().
			occured(evening).
			saveIn(storage);

		List<AggregatedLogEntry> list = storage.getEntries(morning.getDate());
		assertThat(list.size(), equalTo(1));
	}

	@Test
	public void storageShouldNotAggregateEntriesWithNonEqualsChecksum() throws Exception {
		entry().
			checksum("FF").
			saveIn(storage);
		entry().
			checksum("FE").
			saveIn(storage);

		List<AggregatedLogEntry> list = storage.getEntries(today());
		assertThat(list.size(), equalTo(2));
	}

	@Test
	public void storageCanCountEntries() throws Exception {
		LogEntry entry = entry().create();

		storage.writeEntry(entry);

		int count = from(storage).count();
		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanCountEntriesByCriteria() throws LogStorageException, InvalidCriteriaException {
		Date yesterday = yesterday();

		entry().
			occured(yesterday.at("12:23")).
			checksum("2fe").
			saveIn(storage);
		entry().
			checksum("3fe").
			saveIn(storage);

		int count = from(storage).
			date(yesterday).
			count();
		assertThat(count, equalTo(1));

		count = from(storage).
			date(yesterday).
			count();
		assertThat(count, equalTo(1));

		count = from(storage).
			date(yesterday.minusDay(1)).
			count();
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageCanRemoveEntriesByCriteria() throws LogStorageException,
		InvalidCriteriaException {

		Date today = today();
		Date yesterday = yesterday();

		entry().
			occured(today.at("12:35")).
			checksum("FF").
			saveIn(storage);

		entry().
			occured(today.at("12:35")).
			checksum("FE").
			saveIn(storage);

		entry().
			occured(yesterday.at("11:36")).
			checksum("FF").
			saveIn(storage);

		storage.removeEntries("FF", today);

		assertThat(from(storage).count(), equalTo(2));

	}

	protected abstract LogStorage createStorage() throws Exception;
}
