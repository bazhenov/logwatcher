package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

public interface Aggregator {

	Collection<AggregatedLogEntry> aggregate(Iterable<String> entries, Collection<LogEntryMatcher> matchers)
		throws MarshallerException;
}
