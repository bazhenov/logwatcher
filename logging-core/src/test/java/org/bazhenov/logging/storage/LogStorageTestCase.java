package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.farpost.timepoint.Date.*;
import static org.bazhenov.logging.TestSupport.entry;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.fail;

abstract public class LogStorageTestCase {

	private LogStorage storage;

	@BeforeMethod
	public void setUp() throws Exception {
		storage = createStorage();
	}

	@Test
	public void storageCanSaveEntry() throws Exception {
		Date today = today();

		DateTime date = today.at("11:35");
		LogEntry entry = entry().
			occured(date).
			checksum("a").
			attribute("foo", "bar").
			create();
		storage.writeEntry(entry);

		AggregatedEntry aggreagatedEntry = entries().
			date(today).
			findFirst(storage);

		assertThat(aggreagatedEntry.getChecksum(), equalTo("a"));
		assertThat(aggreagatedEntry.getLastTime(), equalTo(date));
	}

	@Test
	public void storageShouldReturnNullIfEntryNotFound()
		throws LogStorageException, InvalidCriteriaException {

		AggregatedEntry entry = entries().applicationId("foo").findFirst(storage);
		assertThat(entry, nullValue());
	}

	@Test
	public void storageCanFindRawEntriesByCriteria() throws LogStorageException,
		InvalidCriteriaException {
		LogEntry first = entry().checksum("fa").saveIn(storage);
		LogEntry second = entry().checksum("fa").saveIn(storage);

		List<LogEntry> entries = entries().checksum("fa").find(storage);

		assertThat(entries.size(), equalTo(2));
	}

	@Test
	public void storageCanWalkByEntries() throws LogStorageException, InvalidCriteriaException {
		entry().checksum("foo").saveIn(storage);
		entry().checksum("foo").saveIn(storage);
		entry().checksum("bar").saveIn(storage);

		CountVisitor<LogEntry> visitor = new CountVisitor<LogEntry>();
		entries().checksum("foo").walk(storage, visitor);
		assertThat(visitor.getCount(), equalTo(2));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		Date date = november(12, 2008);
		DateTime morning = date.at("11:00");
		DateTime evening = date.at("18:05");
		String checksum = "aef";

		entry().
			checksum(checksum).
			occured(morning).
			saveIn(storage);
		entry().
			checksum(checksum).
			occured(evening).
			saveIn(storage);

		List<AggregatedEntry> list = storage.getAggregatedEntries(date, Severity.info);
		assertThat(list.size(), equalTo(1));

		AggregatedEntry entry = list.get(0);

		assertThat(entry.getChecksum(), equalTo(checksum));
	}

	@Test
	public void storageCanFilterEntriesByApplicationId()
		throws LogStorageException, InvalidCriteriaException {
		entry().
			applicationId("frontend").
			saveIn(storage);

		entry().
			applicationId("billing").
			saveIn(storage);

		int count = entries().applicationId("frontend").count(storage);

		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanFilterEntriesByDate() throws LogStorageException, InvalidCriteriaException {
		entry().
			occured(today().at("12:22")).checksum("a").
			saveIn(storage);

		entry().
			occured(yesterday().at("10:00")).checksum("b").
			saveIn(storage);

		int count = entries().date(today(), today()).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(yesterday(), today()).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(today().minusDay(2), today()).
			count(storage);
		assertThat(count, equalTo(2));

		count = entries().date(today(), tomorrow()).
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test(enabled = false)
	public void storageCanMaintainChecksumAliases()
		throws LogStorageException, InvalidCriteriaException {

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

		List<AggregatedEntry> list = entries().
			date(today()).
			findAggregated(storage);
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
	public void storageCanCountEntriesByCriteria()
		throws LogStorageException, InvalidCriteriaException {
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
	public void storageCanCountEntriesBySeverity()
		throws LogStorageException, InvalidCriteriaException {
		entry().
			severity(Severity.warning).
			saveIn(storage);

		int count = entries().
			severity(Severity.warning).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			severity(Severity.info).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			severity(Severity.error).
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageCanRemoveEntriesByCriteria()
		throws LogStorageException, InvalidCriteriaException {

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

		storage.removeEntries("FF");

		assertThat(entries().count(storage), equalTo(1));
		for ( AggregatedEntry entry : storage.getAggregatedEntries(today, Severity.info) ) {
			if ( entry.getChecksum().equals("FF") ) {
				fail("Collection must not contains entries with checksum 'FF'");
			}
		}

	}

	protected abstract LogStorage createStorage() throws Exception;
}
