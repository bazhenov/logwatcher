package org.bazhenov.logging.storage;

import java.util.*;

public class LogEntriesFinder {

	private final LogStorage storage;
	private final List<LogEntryMatcher> criterias = new ArrayList<LogEntryMatcher>();

	public LogEntriesFinder(LogStorage storage) {
		this.storage = storage;
	}

	public int count() throws LogStorageException {
		return storage.countEntries(criterias);
	}
}
