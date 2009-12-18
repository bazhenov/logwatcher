package org.bazhenov.logging.aggregator;

import org.apache.log4j.Logger;
import org.bazhenov.logging.*;

import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

public class SimpleAggregator implements Aggregator {

	private final Logger log = Logger.getLogger(SimpleAggregator.class);

	public Collection<AggregatedLogEntry> aggregate(Iterable<LogEntry> entries,
	                                                Collection<LogEntryMatcher> matchers) {
		Map<String, AggregatedLogEntry> result = new HashMap<String, AggregatedLogEntry>();
		long start = System.currentTimeMillis();
		int size = 0;
		for ( LogEntry entry : entries ) {
			if ( isMatching(entry, matchers) ) {
				if ( result.containsKey(entry.getChecksum()) ) {
					AggregatedLogEntryImpl aggregated = (AggregatedLogEntryImpl) result.get(
						entry.getChecksum());
					aggregated.happensAgain(entry);
				} else {
					result.put(entry.getChecksum(), new AggregatedLogEntryImpl(entry));
				}
			}
			size++;
		}
		long end = System.currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("Processing of " + size + " entries done in " + (end - start) + "ms.");
		}
		return result.values();
	}
}
