package com.farpost.logwatcher;

import com.farpost.logwatcher.LogEntry;

/**
 * Имплементации этого интерфейса занимаются тем что вычисляют контрольную сумму
 * записи лога - {@link com.farpost.logwatcher.LogEntryImpl}. Контрольная сумма используется для того чтобы группировать
 * записи в выдаче.
 */
public interface ChecksumCalculator {

	/**
	 * Вычисляет контрольную сумму записи
	 * @param entry запись
	 * @return контрольная сумма
	 */
	String calculateChecksum(LogEntry entry);
}
