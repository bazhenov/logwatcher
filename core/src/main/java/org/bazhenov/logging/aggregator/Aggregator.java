package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.List;
import java.util.Collection;

public interface Aggregator {

	List<AggregatedLogEntry> aggregate(List<LogEntry> entries, Collection<LogEntryMatcher> matchers);
}
