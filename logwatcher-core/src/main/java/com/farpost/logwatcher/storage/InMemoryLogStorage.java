package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.*;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.farpost.logwatcher.storage.MatcherUtils.isMatching;
import static java.util.stream.Collectors.toList;

/**
 * Реализация {@link LogStorage}, которая хранит все записи в памяти. Потокобезопасна.
 */
public class InMemoryLogStorage implements LogStorage {

	private final List<LogEntry> entries = new ArrayList<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock writeLock = lock.writeLock();
	private final Lock readLock = lock.readLock();
	private final ChecksumCalculator checksumCalculator = new SimpleChecksumCalculator();

	public void writeEntry(final LogEntry entry) throws LogStorageException {
		final LogEntryImpl impl = (LogEntryImpl) entry;
		withLock(writeLock, () -> {
			//if (entry.getChecksum() == null) {
			String checksum = checksumCalculator.calculateChecksum(impl);
			impl.setChecksum(checksum);
			//}
			entries.add(impl);
			return null;
		});
	}

	public int removeOldEntries(final LocalDate date) throws LogStorageException {
		return withLock(writeLock, () -> {
			Iterator<LogEntry> iterator = entries.iterator();
			int removed = 0;
			while (iterator.hasNext()) {
				LogEntry entry = iterator.next();
				if (entry.getDate().before(date.toDate())) {
					iterator.remove();
					removed++;
				}
			}
			return removed;
		});
	}

	public List<LogEntry> findEntries(final Collection<LogEntryMatcher> criteria) {
		return withLock(readLock, () -> entries.stream()
				.filter(entry -> isMatching(entry, criteria))
				.collect(toList())
		);
	}

	public <T> T walk(final Collection<LogEntryMatcher> criteria, final Visitor<LogEntry, T> visitor) {
		withLock(readLock, () -> {
			entries.stream().filter(entry -> isMatching(entry, criteria)).forEach(visitor::visit);
			return null;
		});

		return visitor.getResult();
	}

	public int countEntries(final Collection<LogEntryMatcher> criteria) {
		return withLock(readLock, () -> (int)entries.stream().filter(e -> isMatching(e, criteria)).count());
	}

	public void removeEntriesWithChecksum(final String checksum) throws LogStorageException {
		withLock(writeLock, () -> {
			Iterator<LogEntry> iterator = entries.iterator();
			while (iterator.hasNext()) {
				LogEntry entry = iterator.next();
				if (checksum.equals(checksumCalculator.calculateChecksum(entry))) {
					iterator.remove();
				}
			}
			return null;
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
