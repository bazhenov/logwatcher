package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;

/**
 * Имплементации этого интерфейса фильтруют обьекты типа {@link AggregatedLogEntry}
 */
public interface LogEntryMatcher {

	boolean isMatch(AggregatedLogEntry entry);
}
