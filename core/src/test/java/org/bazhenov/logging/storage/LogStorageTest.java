package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import static com.farpost.timepoint.Date.*;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import static org.bazhenov.logging.TestSupport.entry;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

		AggregatedLogEntry aggreagatedEntry = entries().
			date(today).
			findFirst(storage);

		assertThat(aggreagatedEntry.getSampleEntry(), equalTo(entry));
	}

	@Test
	public void storageMustOrderEntriesByLastOccurenceDate() throws LogStorageException,
		InvalidCriteriaException {
		Date today = today();

		LogEntry thirdEntry = entry().
			occured(today.at("12:55")).
			checksum("a").
			saveIn(storage);

		LogEntry firstEntry = entry().
			occured(today.at("15:55")).
			checksum("b").
			saveIn(storage);

		LogEntry secondEntry = entry().
			occured(today.at("15:53")).
			checksum("c").
			saveIn(storage);

		List<AggregatedLogEntry> list = entries().date(today).from(storage);
		assertThat(list.get(0).getSampleEntry(), equalTo(firstEntry));
		assertThat(list.get(1).getSampleEntry(), equalTo(secondEntry));
		assertThat(list.get(2).getSampleEntry(), equalTo(thirdEntry));
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
	public void storageCanFilterEntriesByApplicationId() throws LogStorageException,
		InvalidCriteriaException {
		entry().
			applicationId("frontend").
			saveIn(storage);

		entry().
			applicationId("billing").
			saveIn(storage);

		int count = entries()
			.applicationId("frontend")
			.count(storage);

		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanMaintainChecksumAliases() throws LogStorageException,
		InvalidCriteriaException {
		entry().
			checksum("foo").
			saveIn(storage);

		entry().
			checksum("bar").
			saveIn(storage);

		storage.createChecksumAlias("foo", "bar");

		int count = entries().
			checksum("bar").
			count(storage);
		assertThat(count, equalTo(1));
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

		int count = entries().count(storage);
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

		int count = entries().
			date(yesterday).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			date(yesterday).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			date(yesterday.minusDay(1)).
			count(storage);
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

		assertThat(entries().count(storage), equalTo(2));

	}

	protected abstract LogStorage createStorage() throws Exception;
}
