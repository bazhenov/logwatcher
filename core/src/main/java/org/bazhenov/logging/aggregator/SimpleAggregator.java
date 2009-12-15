package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

public class SimpleAggregator implements Aggregator {

	public List<AggregatedLogEntry> aggregate(List<LogEntry> entries, Collection<LogEntryMatcher> matchers) {
		List<LogEntry> result = new LinkedList<LogEntry>();
		for ( LogEntry entry : entries ) {
			if ( isMatching(entry, matchers) ) {
				result.add(entry);
			}
		}
		return null;
	}
}
