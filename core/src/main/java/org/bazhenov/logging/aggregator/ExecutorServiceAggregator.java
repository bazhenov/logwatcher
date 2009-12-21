package org.bazhenov.logging.aggregator;

import org.apache.log4j.Logger;
import org.bazhenov.logging.*;
import org.bazhenov.logging.storage.LogEntryMatcher;

import static java.lang.System.currentTimeMillis;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import java.util.concurrent.*;
import java.util.*;

import static java.lang.Thread.interrupted;

public class ExecutorServiceAggregator implements Aggregator {

	private final ExecutorService service;
	private final int batchSize = 500;
	private final Logger log = Logger.getLogger(ExecutorServiceAggregator.class);

	public ExecutorServiceAggregator(ExecutorService service) {
		this.service = service;
	}

	public ExecutorServiceAggregator() {
		this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	}

	public Collection<AggregatedLogEntry> aggregate(Iterable<LogEntry> entries,
	                                                Collection<LogEntryMatcher> matchers) {
		long start = currentTimeMillis();
		List<Future<Collection<AggregatedLogEntry>>> futures = emitTasks(entries, matchers);
		Map<String, AggregatedLogEntry> result = aggregateResults(futures);
		long end = currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("Filtering and aggregating complete in " + (end - start) + "ms.");
		}
		return result.values();
	}

	private Map<String, AggregatedLogEntry> aggregateResults(
		List<Future<Collection<AggregatedLogEntry>>> futures) {
		Map<String, AggregatedLogEntry> result = new HashMap<String, AggregatedLogEntry>();
		long start = currentTimeMillis();
		for ( Future<Collection<AggregatedLogEntry>> future : futures ) {
			try {
				Collection<AggregatedLogEntry> aggregatedEntries = future.get();
				for ( AggregatedLogEntry entry : aggregatedEntries ) {
					String checksum = entry.getSampleEntry().getChecksum();
					if ( result.containsKey(checksum) ) {
						((AggregatedLogEntryImpl) result.get(checksum)).merge(entry);
					} else {
						result.put(checksum, entry);
					}
				}
			} catch ( InterruptedException e ) {
				interrupted();
			} catch ( ExecutionException e ) {
				throw new RuntimeException(e);
			}
		}
		long end = currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("Waiting results and aggregating complete in " + (end - start) + "ms.");
		}
		return result;
	}

	private List<Future<Collection<AggregatedLogEntry>>> emitTasks(Iterable<LogEntry> entries,
	                                                               Collection<LogEntryMatcher> matchers) {
		LinkedList<Future<Collection<AggregatedLogEntry>>> futures = new LinkedList<Future<Collection<AggregatedLogEntry>>>();
		Iterator<LogEntry> iterator = entries.iterator();
		int size = 0;
		long start = currentTimeMillis();
		while ( iterator.hasNext() ) {
			LogEntry[] batch = new LogEntry[batchSize];
			int batchIndex = 0;
			while ( batchIndex < batchSize && iterator.hasNext() ) {
				batch[batchIndex++] = iterator.next();
				size++;
			}
			futures.add(service.submit(new Task(batch, matchers)));
		}
		long end = currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("Emiting tasks complete in " + (end - start) + "ms. (" + size + " entries)");
		}
		return futures;
	}

	class Task implements Callable<Collection<AggregatedLogEntry>> {

		private final LogEntry[] batch;
		private final Collection<LogEntryMatcher> matchers;

		public Task(LogEntry[] batch, Collection<LogEntryMatcher> matchers) {
			this.batch = batch;
			this.matchers = matchers;
		}

		public Collection<AggregatedLogEntry> call() throws Exception {
			Map<String, AggregatedLogEntry> result = new HashMap<String, AggregatedLogEntry>();
			long start = currentTimeMillis();
			int processedCnt = 0;
			for ( LogEntry entry : batch ) {
				if ( entry == null ) {
					continue;
				}
				if ( isMatching(entry, matchers) ) {
					if ( result.containsKey(entry.getChecksum()) ) {
						AggregatedLogEntryImpl aggregated = (AggregatedLogEntryImpl) result.get(
							entry.getChecksum());
						aggregated.happensAgain(entry);
					} else {
						result.put(entry.getChecksum(), new AggregatedLogEntryImpl(entry));
					}
				}
				processedCnt++;
			}
			long end = currentTimeMillis();
			if ( log.isInfoEnabled() ) {
				log.info(
					"Batch processing of " + processedCnt + " entries done in " + (end - start) + "ms.");
			}
			return result.values();
		}
	}
}


