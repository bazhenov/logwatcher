package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;

import java.util.*;

public class LogEntriesFinder {

	private final LogStorage storage;
	private final List<LogEntryMatcher> criterias = new LinkedList<LogEntryMatcher>();

	public LogEntriesFinder(LogStorage storage) {
		this.storage = storage;
	}

	public int count() throws LogStorageException {
		return storage.countEntries(criterias);
	}

	public LogEntriesFinder date(Date date) {
		criterias.add(new DateMatcher(date));
		return this;
	}
}
