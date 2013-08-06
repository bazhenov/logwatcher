package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.CountVisitor;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static com.farpost.logwatcher.storage.LogEntries.entries;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.joda.time.LocalTime.parse;

abstract public class LogStorageTestCase {

	private LogStorage storage;

	private static final LocalDate today = LocalDate.now();
	private static final LocalDate yesterday = LocalDate.now().minusDays(1);
	private static final LocalDate tomorrow = LocalDate.now().plusDays(1);

	@BeforeMethod
	public void setUp() throws Exception {
		storage = createStorage();
	}

	@Test
	public void storageCanSaveEntry() throws Exception {
		DateTime occurredDate = today.toDateTime(parse("11:35"));
		LogEntry entry = entry().
			occurred(occurredDate).
			attribute("foo", "bar").
			create();
		storage.writeEntry(entry);

		LogEntry entryRef = entries().
			date(today).
			find(storage).
			get(0);

		assertThat(entryRef.getChecksum(), notNullValue());
		assertThat(entryRef.getDate(), equalTo(occurredDate.toDate()));
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
			occurred(today.toDateTime(parse("12:22"))).message("a").
			saveIn(storage);

		entry().
			occurred(yesterday.toDateTime(parse("10:00"))).message("b").
			saveIn(storage);

		int count = entries().date(today, today).count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(yesterday, today).count(storage);
		assertThat(count, equalTo(1));

		count = entries().date(today.minusDays(2), tomorrow).count(storage);
		assertThat(count, equalTo(2));

		count = entries().date(today, tomorrow).count(storage);
		assertThat(count, equalTo(1));
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
			occurred(yesterday.toDateTime(parse("12:23"))).
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
			causedBy("java.lang.RuntimeException").
			count(storage);
		assertThat(count, equalTo(1));

		count = entries().
			causedBy("java.lang.Exception").
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
			occurred(today.toDateTime(parse("12:35"))).
			checksum("foo").
			saveIn(storage);

		entry().
			applicationId("application").
			occurred(today.toDateTime(parse("12:35"))).
			checksum("bar").
			saveIn(storage);

		LogEntry entry = entry().
			applicationId("application").
			checksum("foo").
			occurred(yesterday.toDateTime(parse("11:36"))).
			saveIn(storage);

		storage.removeEntriesWithChecksum(entry.getChecksum());

		assertThat(entries().count(storage), equalTo(1));
	}

	@Test
	public void storageCanRemoveOldEntries() throws LogStorageException, InvalidCriteriaException {
		entry().
			applicationId("foo").
			occurred(yesterday.toDateTime(parse("15:32"))).
			saveIn(storage);

		LogEntry todayEntry = entry().
			applicationId("foo").
			occurred(today.toDateTime(parse("16:00"))).
			saveIn(storage);

		int removedEntriesCount = storage.removeOldEntries(today);
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
