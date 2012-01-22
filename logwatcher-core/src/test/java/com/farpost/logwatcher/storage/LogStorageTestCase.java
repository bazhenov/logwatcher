package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.CountVisitor;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static com.farpost.logwatcher.storage.LogEntries.entries;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.joda.time.DateTime.now;
import static org.testng.Assert.fail;

abstract public class LogStorageTestCase {

	private LogStorage storage;

	private final DateTime today = now().withTimeAtStartOfDay();
	private final DateTime yesterday = now().withTimeAtStartOfDay().minusDays(1);
	private final DateTime tomorrow = now().withTimeAtStartOfDay().plusDays(1);

	@BeforeMethod
	public void setUp() throws Exception {
		storage = createStorage();
	}

	@Test
	public void storageCanSaveEntry() throws Exception {
		DateTime date = today.withTime(11, 35, 0, 0);
		LogEntry entry = entry().
			occurred(date).
			attribute("foo", "bar").
			create();
		storage.writeEntry(entry);

		LogEntry entryRef = entries().
			date(today).
			find(storage).
			get(0);

		assertThat(entryRef.getChecksum(), notNullValue());
		assertThat(entryRef.getDate(), equalTo(date));
	}

	@Test
	public void storageShouldReturnNullIfEntryNotFound()
		throws LogStorageException, InvalidCriteriaException {

		List<LogEntry> entries = entries().applicationId("foo").find(storage);
		assertThat(entries.size(), equalTo(0));
	}

	@Test
	public void storageCanFindRawEntriesByCriteria() throws LogStorageException,
		InvalidCriteriaException {
		entry().saveIn(storage);
		entry().saveIn(storage);

		List<LogEntry> entries = entries().date(today).find(storage);
		assertThat(entries.size(), equalTo(2));

		assertThat(entries.get(0).getChecksum(), equalTo(entries.get(1).getChecksum()));
	}

	@Test
	public void storageCanSearchByStackTrace() {
		entry().causedBy(new RuntimeException("first exception")).saveIn(storage);
		entry().causedBy(new RuntimeException("another first exception")).saveIn(storage);
		entry().causedBy(new RuntimeException("second exception")).saveIn(storage);

		List<LogEntry> entries = entries().date(today).contains("first").find(storage);

		assertThat(entries.size(), equalTo(2));
	}

	@Test
	public void storageCanWalkByEntries() throws LogStorageException, InvalidCriteriaException {
		entry().message("bar foo bar").saveIn(storage);
		entry().message("bar foo bar").saveIn(storage);
		entry().message("bar bar bar").saveIn(storage);

		CountVisitor<LogEntry> visitor = new CountVisitor<LogEntry>();
		int count = entries().
			date(today).
                contains("foo").
                walk(storage, visitor);

		assertThat(count, equalTo(2));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		DateTime date = new DateTime(2008, 11, 12, 0, 0);
		DateTime morning = date.withTime(11, 0, 0, 0);
		DateTime evening = date.withTime(18, 5, 0, 0);

		entry().
			applicationId("search").
			message("Error in search").
			occurred(morning).
			saveIn(storage);
		entry().
			applicationId("billing").
			message("Error in billing").
			occurred(evening).
			saveIn(storage);
		entry().
			applicationId("billing").
			message("Error in billing").
			occurred(evening).
			saveIn(storage);

		List<AggregatedEntry> list = storage.getAggregatedEntries("billing", date, Severity.info);
		assertThat(list.size(), equalTo(1));

		AggregatedEntry entry = list.get(0);
		assertThat(entry.getMessage(), equalTo("Error in billing"));
		assertThat(entry.getCount(), equalTo(2));

		list = storage.getAggregatedEntries("search", date, Severity.info);
		assertThat(list.size(), equalTo(1));

		entry = list.get(0);
		assertThat(entry.getMessage(), equalTo("Error in search"));
		assertThat(entry.getCount(), equalTo(1));
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

		count = entries().applicationId("fronTend").count(storage);
		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanFilterEntriesByDate() throws LogStorageException, InvalidCriteriaException {
		entry().
			occurred(today.withTime(12, 22, 0, 0)).message("a").
			saveIn(storage);

		entry().
			occurred(yesterday.withTime(10, 0, 0, 0)).message("b").
                saveIn(storage);

		int count = entries().date(today, today).
                count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(yesterday, today).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(today.minusDays(2), today).
			count(storage);
		assertThat(count, equalTo(2));

		count = entries().date(today, tomorrow).
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageShouldNotAggregateEntriesWithNonEqualsChecksum() throws Exception {
		entry().
			applicationId("appl").
			checksum("foo").
			saveIn(storage);
		entry().
			applicationId("appl").
			checksum("bar").
			saveIn(storage);

		List<AggregatedEntry> list = storage.getAggregatedEntries("appl", today, Severity.debug);
		assertThat(list.size(), equalTo(2));
	}

	@Test
	public void storageCanReturnUniqueApplicationIdSet() {
		entry().
			applicationId("foo").
			occurred(today.withTime(12, 0, 0, 0)).
			saveIn(storage);
		entry().
			applicationId("bar").
			occurred(today.withTime(12, 0, 0, 0)).
			saveIn(storage);
		entry().
			applicationId("baz").
			occurred(yesterday.withTime(13, 52, 0, 0)).
			saveIn(storage);

		Set<String> ids = storage.getUniquieApplicationIds(today);
		assertThat(ids.size(), equalTo(2));
		assertThat(ids, hasItem("foo"));
		assertThat(ids, hasItem("bar"));
	}

	@Test
	public void storageCanCountEntries() throws Exception {
		LogEntry entry = entry().create();

		storage.writeEntry(entry);

		int count = entries().count(storage);
		assertThat(count, equalTo(1));
	}

	@Test
	public void storageCanCountEntriesByCriteria() {
		entry().
			occurred(yesterday.withTime(12, 23, 0 ,0)).
			checksum("ffe").
			message("foo").
			saveIn(storage);
		entry().
			message("bar").
			checksum("fff").
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
			date(yesterday.minusDays(1)).
			count(storage);
		assertThat(count, equalTo(0));

		count = entries().
			checksum("ffe").
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageCanFindEntriesByAttributeValue() {
		LogEntry entry = entry().attribute("foo", "bar").saveIn(storage);

		List<LogEntry> entries = entries().attribute("foo", "bar").find(storage);

		assertThat(entries.size(), equalTo(1));
		assertThat(entries.get(0), equalTo(entry));
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
	public void storageCanFindEntriesByExceptionType() throws LogStorageException, InvalidCriteriaException {
		entry().causedBy(new RuntimeException(new Exception())).saveIn(storage);

		int count = entries().
			causedBy("RuntimeException").
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			causedBy("Exception").
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			causedBy("OutOfMemoryError").
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageCanRemoveEntriesWithSpecifiedChecksum()
		throws LogStorageException, InvalidCriteriaException {

		entry().
			applicationId("application").
			occurred(today.withTime(12, 35, 0, 0)).
			checksum("foo").
			saveIn(storage);

		entry().
			applicationId("application").
                occurred(today.withTime(12, 35, 0, 0)).
			checksum("bar").
			saveIn(storage);

		LogEntry entry = entry().
			applicationId("application").
			checksum("foo").
            occurred(yesterday.withTime(11, 36, 0, 0)).
			saveIn(storage);

		storage.removeEntriesWithChecksum(entry.getChecksum());

		assertThat(entries().count(storage), equalTo(1));
		for (AggregatedEntry en : storage.getAggregatedEntries("application", today, Severity.info)) {
			if (en.getChecksum().equals(entry.getChecksum())) {
				fail("Collection must not contains entries with checksum '" + entry.getChecksum() + "'");
			}
		}
	}

	@Test
	public void storageCanRemoveOldEntries() throws LogStorageException, InvalidCriteriaException {
		entry().
			applicationId("foo").
            occurred(yesterday.withTime(15, 32, 0, 0)).
			saveIn(storage);

		LogEntry todayEntry = entry().
			applicationId("foo").
            occurred(today.withTime(16, 0, 0, 0)).
			saveIn(storage);

		int removedEntriesCount = storage.removeOldEntries(today.toDateMidnight());
		assertThat(removedEntriesCount, equalTo(1));

		List<LogEntryMatcher> criteria = entries().
			applicationId("foo").
			criterias();
		List<LogEntry> entries = storage.findEntries(criteria);
		assertThat(entries.size(), equalTo(1));
		assertThat(entries, hasItem(todayEntry));
	}

	protected abstract LogStorage createStorage() throws Exception;
}
