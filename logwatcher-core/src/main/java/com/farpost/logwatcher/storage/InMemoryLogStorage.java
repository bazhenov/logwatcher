package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.*;
import com.farpost.timepoint.Date;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
		final LogEntryImpl impl = (LogEntryImpl) entry;
		withLock(writeLock, new Callable<Void>() {

			public Void call() {
				String checksum = checksumCalculator.calculateChecksum(impl);
				impl.setChecksum(checksum);
				entries.add(impl);
				return null;
			}
		});
	}

	public void createChecksumAlias(String checksum, String alias) {
		throw new UnsupportedOperationException();
	}

	public void removeOldEntries(final Date date) throws LogStorageException {
		withLock(writeLock, new Callable<Void>() {

			public Void call() throws Exception {
				Iterator<LogEntry> iterator = entries.iterator();
				while (iterator.hasNext()) {
					LogEntry entry = iterator.next();
					if (entry.getDate().getDate().lessThan(date)) {
						iterator.remove();
					}
				}
				return null;
			}
		});
	}

	public List<LogEntry> findEntries(final Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		return withLock(readLock, new Callable<List<LogEntry>>() {

			public List<LogEntry> call() throws Exception {
				List<LogEntry> result = new ArrayList<LogEntry>();
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criterias)) {
						result.add(entry);
					}
				}
				return result;
			}
		});
	}

	public List<AggregatedEntry> findAggregatedEntries(final Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		return withLock(readLock, new Callable<List<AggregatedEntry>>() {

			public List<AggregatedEntry> call() throws Exception {
				Map<String, AggregatedEntry> result = new HashMap<String, AggregatedEntry>();
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criterias)) {
						AggregatedEntryImpl aggregated = (AggregatedEntryImpl) result.get(entry.getChecksum());
						if (aggregated == null) {
							result.put(entry.getChecksum(), new AggregatedEntryImpl(entry));
						} else {
							aggregated.happensAgain(1, entry.getDate());
						}
					}
				}
				return new ArrayList<AggregatedEntry>(result.values());
			}
		});
	}

	public List<AggregatedEntry> getAggregatedEntries(Date date, Severity severity)
		throws LogStorageException, InvalidCriteriaException {

		return LogEntries.entries().
			date(date).
			severity(severity).
			findAggregated(this);
	}

	public List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date, Severity severity)
		throws LogStorageException, InvalidCriteriaException {

		return LogEntries.entries().
			applicationId(applicationId).
			date(date).
			severity(severity).
			findAggregated(this);
	}

	public void walk(final Collection<LogEntryMatcher> criterias, final Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {

		withLock(readLock, new Callable<Void>() {

			public Void call() throws Exception {
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criterias)) {
						visitor.visit(entry);
					}
				}
				return null;
			}
		});
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		return findAggregatedEntries(criterias).size();
	}

	public void removeEntries(final String checksum) throws LogStorageException {
		withLock(writeLock, new Callable<Void>() {

			public Void call() throws Exception {
				Iterator<LogEntry> iterator = entries.iterator();
				while (iterator.hasNext()) {
					LogEntry entry = iterator.next();
					if (checksum.equals(entry.getChecksum())) {
						iterator.remove();
					}
				}
				return null;
			}
		});
	}

	private static <T> T withLock(Lock lock, Callable<T> task) throws LogStorageException {
		lock.lock();
		try {
			return task.call();
		} catch (Exception e) {
			throw new LogStorageException(e);
		} finally {
			lock.unlock();
		}
	}
}
