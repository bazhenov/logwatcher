package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;

public interface LogEntryMatcher {

	boolean isMatch(AggregatedLogEntry entry);
}
