package com.farpost.logwatcher;

import org.bazhenov.logging.LogEntry;

/**
 * Имплементации этого интерфейса занимаются тем что вычисляют контрольную сумму
 * записи лога - {@link org.bazhenov.logging.LogEntryImpl}. Контрольная сумма используется для того чтобы группировать
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
