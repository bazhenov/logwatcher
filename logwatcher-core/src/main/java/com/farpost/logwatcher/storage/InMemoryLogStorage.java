package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.*;
import org.joda.time.LocalDate;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.farpost.logwatcher.storage.LogEntries.entries;

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

	public int removeOldEntries(final LocalDate date) throws LogStorageException {
		return withLock(writeLock, new Callable<Integer>() {

			public Integer call() throws Exception {
				Iterator<LogEntry> iterator = entries.iterator();
				int removed = 0;
				while (iterator.hasNext()) {
					LogEntry entry = iterator.next();
					if (entry.getDate().toLocalDate().isBefore(date)) {
						iterator.remove();
						removed++;
					}
				}
				return removed;
			}
		});
	}

	public List<LogEntry> findEntries(final Collection<LogEntryMatcher> criteria) {
		return withLock(readLock, new Callable<List<LogEntry>>() {

			public List<LogEntry> call() throws Exception {
				List<LogEntry> result = new ArrayList<LogEntry>();
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criteria)) {
						result.add(entry);
					}
				}
				return result;
			}
		});
	}

	@Override
	public Set<String> getUniqueApplicationIds(LocalDate date) {
		List<LogEntry> entries = findEntries(entries().date(date).criterias());
		HashSet<String> applicationIds = new HashSet<String>();
		for (LogEntry entry : entries) {
			applicationIds.add(entry.getApplicationId());
		}
		return applicationIds;
	}

	public <T> T walk(final Collection<LogEntryMatcher> criteria, final Visitor<LogEntry, T> visitor) {
		withLock(readLock, new Callable<Void>() {

			public Void call() throws Exception {
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criteria)) {
						visitor.visit(entry);
					}
				}
				return null;
			}
		});

		return visitor.getResult();
	}

	public int countEntries(final Collection<LogEntryMatcher> criteria) {
		return withLock(readLock, new Callable<Integer>() {
			public Integer call() throws Exception {
				int result = 0;
				for (LogEntry entry : entries) {
					if (MatcherUtils.isMatching(entry, criteria)) {
						result++;
					}
				}
				return result;
			}
		});
	}

	public void removeEntriesWithChecksum(final String checksum) throws LogStorageException {
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
