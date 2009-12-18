package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;
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
					}else{
						throw new RuntimeException("Ooops");
					}
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	public List<AggregatedLogEntry> getEntries(Collection<LogEntryMatcher> criterias)
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

	public void removeEntries(String checksum, Date date) throws LogStorageException {
		writeLock.lock();
		try {
			Map<String, AggregatedLogEntryImpl> byDate = entriesByDay.get(date);
			if ( byDate != null ) {
				AggregatedLogEntryImpl entry = byDate.get(checksum);
				if ( entry != null ) {
					byDate.remove(checksum);
					entries.remove(entry);
				}
			}
		} finally {
			writeLock.unlock();
		}
	}
}
