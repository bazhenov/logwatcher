package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.aggregator.SimpleAggregator;

import static com.farpost.timepoint.Date.today;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасная.
 */
public class InMemoryLogStorage implements LogStorage {

	private final List<LogEntry> entries = new ArrayList<LogEntry>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock writeLock = lock.writeLock();
	private final Lock readLock = lock.readLock();
	private final Comparator<? super AggregatedLogEntry> comparator = new AggregatedLogEntryComparator();

	public void writeEntry(LogEntry entry) throws LogStorageException {
		writeLock.lock();
		try {
			entries.add(entry);
		} finally {
			writeLock.unlock();
		}
	}

	public void createChecksumAlias(String checksum, String alias) {
		writeLock.lock();
		try {
			throw new UnsupportedOperationException();
		} finally {
			writeLock.unlock();
		}
	}

	public List<AggregatedLogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		Map<String, AggregatedLogEntry> result = new HashMap<String, AggregatedLogEntry>();
		for ( LogEntry entry : entries ) {
			AggregatedLogEntryImpl aggregated = (AggregatedLogEntryImpl) result.get(entry.getChecksum());
			if ( aggregated == null ) {
				result.put(entry.getChecksum(), new AggregatedLogEntryImpl(entry));
			} else {
				aggregated.happensAgain(entry);
			}
		}
		List<AggregatedLogEntry> list = new ArrayList<AggregatedLogEntry>(result.values());
		Collections.sort(list, comparator);
		return list;
	}

	public List<AggregatedEntry> getAggregatedEntries(Date date, Severity severity)
		throws LogStorageException {
		List<LogEntryMatcher> matchers = entries().
			date(date).
			severity(severity).
			criterias();
		try {
			List<AggregatedLogEntry> entries = findEntries(matchers);

			return map(entries, new MapOperation<AggregatedLogEntry, AggregatedEntry>() {
				public AggregatedEntry map(AggregatedLogEntry input) {
					LogEntry entry = input.getSampleEntry();
					return new AggregatedEntryImpl(entry.getMessage(), entry.getChecksum(),
						entry.getSeverity(), input.getCount(), input.getLastTime(), entry.getCause());
				}
			});
		} catch ( InvalidCriteriaException e ) {
			throw new LogStorageException(e);
		}
	}

	public void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {
		for ( LogEntry entry : entries ) {
			if ( isMatching(entry, criterias) ) {
				visitor.visit(entry);
			}
		}
	}

	public static <I, O> List<O> map(Collection<I> input, MapOperation<I, O> op) {
		List<O> result = new ArrayList<O>(input.size());
		for ( I i : input ) {
			result.add(op.map(i));
		}
		return result;
	}

	public int countEntries(Collection<LogEntryMatcher> criterias) throws LogStorageException {
		readLock.lock();
		try {
			int matches = 0;
			for ( LogEntry entry : entries ) {
				if ( isMatching(entry, criterias) ) {
					matches++;
				}
			}
			return matches;
		} finally {
			readLock.unlock();
		}
	}

	public void removeEntries(String checksum) throws LogStorageException {
		writeLock.lock();
		try {
			Iterator<LogEntry> iterator = entries.iterator();
			while ( iterator.hasNext() ) {
				LogEntry entry = iterator.next();
				if ( checksum.equals(entry.getChecksum()) ) {
					iterator.remove();
				}
			}
		} finally {
			writeLock.unlock();
		}
	}
}
