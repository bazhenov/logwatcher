package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасная.
 */
public class InMemoryLogStorage implements LogStorage {

	private final Map<Date, Map<String, AggregatedLogEntryImpl>> entriesByDay =
		new HashMap<Date, Map<String, AggregatedLogEntryImpl>>();
	private final List<AggregatedLogEntry> entries = new ArrayList<AggregatedLogEntry>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	public void writeEntry(LogEntry entry) throws LogStorageException {
		Lock l = lock.writeLock();
		l.lock();
		try {
			Date date = entry.getDate().getDate();
			String checksum = entry.getChecksum();

			Map<String, AggregatedLogEntryImpl> dayEntries;
			if ( entriesByDay.containsKey(date) ) {
				dayEntries = entriesByDay.get(date);
			}else{
				dayEntries = new HashMap<String, AggregatedLogEntryImpl>();
				entriesByDay.put(date, dayEntries);
			}

			if ( dayEntries.containsKey(checksum) ) {
				AggregatedLogEntryImpl aggregate = dayEntries.get(checksum);
				aggregate.setLastTime(entry.getDate());
				aggregate.incrementCount();
			}else{
				AggregatedLogEntryImpl aggregatedEntry = new AggregatedLogEntryImpl(entry);
				dayEntries.put(entry.getChecksum(), aggregatedEntry);
				entries.add(aggregatedEntry);
			}
		} finally {
			l.unlock();
		}
	}

	public List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException {
		Lock l = lock.readLock();
		l.lock();
		try {
			Map<String, AggregatedLogEntryImpl> dayEntries = entriesByDay.get(date);
			return dayEntries == null
				? new ArrayList<AggregatedLogEntry>(0)
				: new ArrayList<AggregatedLogEntry>(dayEntries.values());
		} finally {
			l.unlock();
		}
	}

	public int getEntryCount(Date date) throws LogStorageException {
		Lock l = lock.readLock();
		l.lock();
		try {
			Map<String, AggregatedLogEntryImpl> dayEntries = entriesByDay.get(date);
			return dayEntries == null
				? 0
				: dayEntries.size();
		} finally {
			l.unlock();
		}
	}

	public int countEntries(Collection<LogEntryMatcher> criterias) throws LogStorageException {
		Lock l = lock.readLock();
		l.lock();
		try {
			return entries.size();
		} finally {
			l.unlock();
		}
	}
}
