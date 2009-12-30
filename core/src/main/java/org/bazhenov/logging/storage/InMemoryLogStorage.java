package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;

import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасная.
 */
public class InMemoryLogStorage implements LogStorage {

	private final Map<Date, Map<String, AggregatedLogEntryImpl>> entriesByDay = new HashMap<Date, Map<String, AggregatedLogEntryImpl>>();
	private final List<AggregatedLogEntry> entries = new ArrayList<AggregatedLogEntry>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private Lock readLock = lock.readLock();
	private Comparator<? super AggregatedLogEntry> comparator = new AggregatedLogEntryComparator();

	public void writeEntry(LogEntry entry) throws LogStorageException {
		writeLock.lock();
		try {
			Date date = entry.getDate().getDate();
			String checksum = entry.getChecksum();

			Map<String, AggregatedLogEntryImpl> dayEntries;
			if ( entriesByDay.containsKey(date) ) {
				dayEntries = entriesByDay.get(date);
			} else {
				dayEntries = new HashMap<String, AggregatedLogEntryImpl>();
				entriesByDay.put(date, dayEntries);
			}

			if ( dayEntries.containsKey(checksum) ) {
				AggregatedLogEntryImpl aggregate = dayEntries.get(checksum);
				aggregate.happensAgain(entry);
			} else {
				AggregatedLogEntryImpl aggregatedEntry = new AggregatedLogEntryImpl(entry);
				dayEntries.put(entry.getChecksum(), aggregatedEntry);
				entries.add(aggregatedEntry);
			}
		} finally {
			writeLock.unlock();
		}
	}

	public void createChecksumAlias(String checksum, String alias) {
		writeLock.lock();
		try {
			for ( Map<String, AggregatedLogEntryImpl> entriesForDate : entriesByDay.values() ) {
				AggregatedLogEntryImpl entry = entriesForDate.get(checksum);
				if ( entry != null ) {
					entriesForDate.remove(checksum);
					if ( entriesForDate.containsKey(alias) ) {
						entriesForDate.get(alias).incrementCount(entry.getCount());
					} else {
						throw new RuntimeException("Ooops");
					}
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	public List<AggregatedLogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		List<AggregatedLogEntry> result = new ArrayList<AggregatedLogEntry>();
		for ( AggregatedLogEntry entry : entries ) {
			if ( isMatching(entry, criterias) ) {
				result.add(entry);
			}
		}
		Collections.sort(result, comparator);
		return result;
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
			for ( AggregatedLogEntry entry : entries ) {
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
			for ( Map<String, AggregatedLogEntryImpl> row : entriesByDay.values() ) {
				AggregatedLogEntryImpl entry = row.remove(checksum);
				if ( entry != null ) {
					entries.remove(entry);
				}
			}
		} finally {
			writeLock.unlock();
		}
	}
}
