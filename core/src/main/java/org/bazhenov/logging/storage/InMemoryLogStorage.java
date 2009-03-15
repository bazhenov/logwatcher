package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасная.
 */
public class InMemoryLogStorage implements LogStorage {

	private final Map<Date, Map<String, AggregatedLogEntryImpl>> entries =
		new HashMap<Date, Map<String, AggregatedLogEntryImpl>>();
	ReadWriteLock lock = new ReentrantReadWriteLock();

	public void writeEntry(LogEntry entry) throws LogStorageException {
		Lock l = lock.writeLock();
		try {
			l.lockInterruptibly();
			Date date = entry.getDate().getDate();
			String checksum = entry.getChecksum();

			Map<String, AggregatedLogEntryImpl> dayEntries;
			if ( entries.containsKey(date) ) {
				dayEntries = entries.get(date);
			}else{
				dayEntries = new HashMap<String, AggregatedLogEntryImpl>();
				entries.put(date, dayEntries);
			}

			if ( dayEntries.containsKey(checksum) ) {
				AggregatedLogEntryImpl aggregate = dayEntries.get(checksum);
				aggregate.setLastTime(entry.getDate());
				aggregate.incrementCount();
			}else{
				dayEntries.put(entry.getChecksum(), new AggregatedLogEntryImpl(entry));
			}
		} catch ( InterruptedException e ) {
			throw new LogStorageException(e);
		} finally {
			l.unlock();
		}
	}

	public List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException {
		Map<String, AggregatedLogEntryImpl> dayEntries = entries.get(date);
		return dayEntries == null
			? new ArrayList<AggregatedLogEntry>(0)
			: new ArrayList<AggregatedLogEntry>(dayEntries.values());
	}

	public int getEntryCount(Date date) throws LogStorageException {
		Lock l = lock.readLock();
		try {
			l.lockInterruptibly();
			Map<String, AggregatedLogEntryImpl> dayEntries = entries.get(date);
			return dayEntries == null
				? 0
				: dayEntries.size();
		} catch ( InterruptedException e ) {
			throw new LogStorageException(e);
		} finally {
			l.unlock();
		}
	}
}
