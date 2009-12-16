package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

public interface Aggregator {

	List<AggregatedLogEntry> aggregate(Iterable<LogEntry> entries, Collection<LogEntryMatcher> matchers);
}
