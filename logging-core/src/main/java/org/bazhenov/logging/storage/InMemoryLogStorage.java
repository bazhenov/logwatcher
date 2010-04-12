package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасна.
 */
public class InMemoryLogStorage implements LogStorage {

	private final List<LogEntry> entries = new ArrayList<LogEntry>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock writeLock = lock.writeLock();
	private final Lock readLock = lock.readLock();
	private final ChecksumCalculator checksumCalculator = new SimpleChecksumCalculator();

	public void writeEntry(LogEntry entry) throws LogStorageException {
		writeLock.lock();
		try {
			String checksum = checksumCalculator.calculateChecksum(entry);
			entry.setChecksum(checksum);
			entries.add(entry);
		} finally {
			writeLock.unlock();
		}
	}

	public void createChecksumAlias(String checksum, String alias) {
		throw new UnsupportedOperationException();
	}

	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		readLock.lock();
		try {
			List<LogEntry> result = new ArrayList<LogEntry>();
			for ( LogEntry entry : entries ) {
				if ( isMatching(entry, criterias) ) {
					result.add(entry);
				}
			}
			return result;
		} finally {
			readLock.unlock();
		}
	}

	public List<AggregatedEntry> findAggregatedEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		Map<String, AggregatedEntry> result = new HashMap<String, AggregatedEntry>();
		readLock.lock();
		try {
			for ( LogEntry entry : entries ) {
				if ( isMatching(entry, criterias) ) {
					AggregatedEntryImpl aggregated = (AggregatedEntryImpl) result.get(entry.getChecksum());
					if ( aggregated == null ) {
						result.put(entry.getChecksum(), new AggregatedEntryImpl(entry));
					} else {
						aggregated.happensAgain(1, entry.getDate());
					}
				}
			}
		} finally {
			readLock.unlock();
		}
		return new ArrayList<AggregatedEntry>(result.values());
	}

	public List<AggregatedEntry> getAggregatedEntries(Date date, Severity severity)
		throws LogStorageException {
		List<LogEntryMatcher> matchers = entries().
			date(date).
			severity(severity).
			criterias();
		try {
			return findAggregatedEntries(matchers);
		} catch ( InvalidCriteriaException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date,
	                                                  Severity severity) throws LogStorageException {
		List<LogEntryMatcher> matchers = entries().
			applicationId(applicationId).
			date(date).
			severity(severity).
			criterias();
		try {
			return findAggregatedEntries(matchers);
		} catch ( InvalidCriteriaException e ) {
			throw new LogStorageException(e);
		}
	}

	public void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {
		readLock.lock();
		try {
			for ( LogEntry entry : entries ) {
				if ( isMatching(entry, criterias) ) {
					visitor.visit(entry);
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		return findAggregatedEntries(criterias).size();
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
