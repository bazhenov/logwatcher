package com.farpost.logwatcher.aggregator;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.AggregatedEntryImpl;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.farpost.logwatcher.storage.MatcherUtils.isMatching;

public class SimpleAggregator implements Aggregator {

	private final Logger log = LoggerFactory.getLogger(SimpleAggregator.class);
	private final Marshaller marshaller;

	public SimpleAggregator(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Collection<AggregatedEntry> aggregate(Iterable<byte[]> entries, Collection<LogEntryMatcher> matchers) {
		Map<String, AggregatedEntry> result = new HashMap<String, AggregatedEntry>();
		long start = System.currentTimeMillis();
		int size = 0;
		for (byte[] marshalledEntry : entries) {
			LogEntry entry = marshaller.unmarshall(marshalledEntry);
			if (isMatching(entry, matchers)) {
				if (result.containsKey(entry.getChecksum())) {
					AggregatedEntryImpl impl = (AggregatedEntryImpl) result.get(entry.getChecksum());
					impl.happensAgain(1, entry.getDate());
				} else {
					result.put(entry.getChecksum(), new AggregatedEntryImpl(entry));
				}
			}
			size++;
		}
		long end = System.currentTimeMillis();
		if (log.isInfoEnabled()) {
			log.info("Processing of " + size + " entries done in " + (end - start) + "ms.");
		}
		return result.values();
	}
}
