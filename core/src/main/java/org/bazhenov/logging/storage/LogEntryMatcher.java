package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

/**
 * Имплементации этого интерфейса фильтруют обьекты типа {@link AggregatedLogEntry}
 */
public interface LogEntryMatcher {

	boolean isMatch(AggregatedLogEntry entry);
	boolean isMatch(LogEntry entry);
}
