package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedLogEntry;
import com.farpost.logwatcher.LogEntry;

/**
 * Имплементации этого интерфейса фильтруют обьекты типа {@link LogEntry}.
 * <p />
 * <strong>Внимание!</strong> Все имплементации данного интерфейса должна быть потокобезопасны.
 *
 * @see DateMatcher
 * @see ChecksumMatcher
 * @see AttributeValueMatcher
 */
public interface LogEntryMatcher {

	@Deprecated
	boolean isMatch(AggregatedLogEntry entry);

	/**
	 * Проверяет подпадает ли заданная запись под условия отбора.
	 *
	 * @param entry проверяемая запись
	 * @return подпадает ли запись под условия отбора
	 */
	boolean isMatch(LogEntry entry);
}
