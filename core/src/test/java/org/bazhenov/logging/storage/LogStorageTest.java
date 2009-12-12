package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import static com.farpost.timepoint.Date.*;
import static com.farpost.timepoint.Date.today;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.TestSupport.entry;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.accessibility.AccessibleValue;
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

		List<AggregatedLogEntry> list = entries().date(today).find(storage);
		assertThat(list.get(0).getSampleEntry(), equalTo(firstEntry));
		assertThat(list.get(1).getSampleEntry(), equalTo(secondEntry));
		assertThat(list.get(2).getSampleEntry(), equalTo(thirdEntry));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		Date date = november(12, 2008);
		DateTime morning = date.at("11:00");
		DateTime evening = date.at("18:05");

		entry().
			occured(morning).
			saveIn(storage);
		entry().
			occured(evening).
			saveIn(storage);

		List<AggregatedLogEntry> list = entries().
			date(date).
			find(storage);
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
	public void storageCanFilterEntriesByDate() throws LogStorageException, InvalidCriteriaException {
		entry().
			occured(today().at("12:22")).
			saveIn(storage);

		entry().
			occured(yesterday().at("10:00")).
			saveIn(storage);

		int count = entries()
			.date(today(), today()).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries()
			.date(yesterday(), today()).
			count(storage);
		assertThat(count, equalTo(1));

		count = entries()
			.date(today().minusDay(2), today()).
			count(storage);
		assertThat(count, equalTo(2));

		count = entries()
			.date(today(), tomorrow()).
			count(storage);
		assertThat(count, equalTo(0));
	}

	@Test
	public void storageCanStoreAttributes() throws LogStorageException, InvalidCriteriaException {
		entry().
			attribute("user", "john").
			attribute("machine", "host1").
			saveIn(storage);

		entry().
			attribute("user", "john").
			saveIn(storage);
		entry().
			attribute("user", "christin").
			saveIn(storage);
		entry().
			attribute("machine", "host2").
			attribute("user", "david").
			saveIn(storage);

		AggregatedLogEntry entry = entries().
			date(today()).
			findFirst(storage);
		assertThat(entry.getAttributes().size(), equalTo(2));

		AttributeValue[] array = entry.getAttributes().get("user").toArray();
		assertThat(array, equalTo(new AttributeValue[] {
			new AttributeValue("john", 2),
			new AttributeValue("christin", 1),
			new AttributeValue("david", 1)
		}));

		array = entry.getAttributes().get("machine").toArray();
		assertThat(array, equalTo(new AttributeValue[] {
			new AttributeValue("host1", 1),
			new AttributeValue("host2", 1)
		}));
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

		List<AggregatedLogEntry> list = entries().
			date(today()).
			find(storage);
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
	public void storageCanCountEntriesBySeverity() throws LogStorageException, InvalidCriteriaException {
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
