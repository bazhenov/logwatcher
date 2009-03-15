package org.bazhenov.logging.storage;

import static com.farpost.timepoint.Date.november;
import com.farpost.timepoint.DateTime;
import static com.farpost.timepoint.DateTime.now;
import org.bazhenov.logging.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;

import java.util.*;

abstract public class LogStorageTest {

	@Test
	public void storageCanSaveEntry() throws Exception {
		LogStorage storage = createStorage();
		DateTime date = now();

		Cause cause = new Cause("type", "message", "###");
		LogEntry entry = new LogEntry(date, "group", "message", Severity.debug, "34fd", cause);

		storage.writeEntry(entry);

		assertThat(storage.getEntryCount(date.getDate()), equalTo(1));
		assertThat(storage.getEntryCount(date.plusDay(1).getDate()), equalTo(0));
	}

	@Test
	public void storageCanGetAggregatedEntries() throws Exception {
		LogStorage storage = createStorage();
		DateTime morning = november(12, 2008).at(11, 00);
		DateTime evening = november(12, 2008).at(18, 05);

		String checksum = "1f";
		LogEntry entry1 = new LogEntry(morning, "group", "message", Severity.debug, checksum);
		LogEntry entry2 = new LogEntry(evening, "group", "message", Severity.debug, checksum);

		storage.writeEntry(entry1);
		storage.writeEntry(entry2);

		List<AggregatedLogEntry> list = storage.getEntries(morning.getDate());

		assertThat(list.size(), equalTo(1));
	}

	protected abstract LogStorage createStorage() throws Exception;
}
