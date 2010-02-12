package org.bazhenov.logging.aggregator;

import org.apache.log4j.Logger;
import org.bazhenov.logging.*;

import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

public class SimpleAggregator implements Aggregator {

	private final Logger log = Logger.getLogger(SimpleAggregator.class);
	private final Marshaller marshaller;

	public SimpleAggregator(Marshaller marshaller) {

		this.marshaller = marshaller;
	}

	public Collection<AggregatedEntry> aggregate(Iterable<String> entries,
	                                             Collection<LogEntryMatcher> matchers)
		throws MarshallerException {

		Map<String, AggregatedEntry> result = new HashMap<String, AggregatedEntry>();
		long start = System.currentTimeMillis();
		int size = 0;
		for ( String marshalledEntry : entries ) {
			LogEntry entry = marshaller.unmarshall(marshalledEntry);
			if ( isMatching(entry, matchers) ) {
				if ( result.containsKey(entry.getChecksum()) ) {
					AggregatedEntryImpl impl = (AggregatedEntryImpl) result.get(entry.getChecksum());
					impl.happensAgain(1, entry.getDate());
				} else {
					result.put(entry.getChecksum(), new AggregatedEntryImpl(entry));
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
