package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.*;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

public class SimpleAggregator implements Aggregator {

	public Collection<AggregatedLogEntry> aggregate(Iterable<LogEntry> entries, Collection<LogEntryMatcher> matchers) {
		Map<String, AggregatedLogEntry> result = new HashMap<String, AggregatedLogEntry>();
		for ( LogEntry entry : entries ) {
			if ( isMatching(entry, matchers) ) {
				if ( result.containsKey(entry.getChecksum()) ) {
					AggregatedLogEntryImpl aggregated = (AggregatedLogEntryImpl) result.get(entry.getChecksum());
					aggregated.happensAgain(entry);
				}else{
					result.put(entry.getChecksum(), new AggregatedLogEntryImpl(entry));
				}
			}
		}
		return null;
	}
}
