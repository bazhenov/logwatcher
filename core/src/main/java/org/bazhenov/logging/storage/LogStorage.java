package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.AggregatedLogEntry;

import java.util.*;

/**
 * Имплементации этого интерфейса сохраняют обьекты типа LogEntry в постоянном хранилище? будь то
 * реляционная база данных или XML файл.
 */
public interface LogStorage {

	/**
	 * Сохраняет обьект {@link LogEntry} в постоянном хранилище.
	 * <p/>
	 * Учтите что сохранение не влияет на обьект LogEntry. Ему не выдается никакого
	 * идентификационного номера. Если вы передадите один и тот же LogEntry в этот метод два раза,
	 * то произойдет две записи в постоянное хранилище.
	 *
	 * @param entry запись лога
	 * @throws LogStorageException в случае внутренней ошибки
	 */
	void writeEntry(LogEntry entry) throws LogStorageException;

	/**
	 * Возвращает колличество записей в хранилище за указанную дату
	 *
	 * @param date дата
	 * @return колличество записей
	 * @throws LogStorageException в случае внутренней ошибки
	 * @deprecated
	 */
	int getEntryCount(Date date) throws LogStorageException;

	List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException;

	/**
	 * Подсчитывает колличество записей в хранилище с заданными условиями
	 *
	 * @param criterias условия отбора записей
	 * @throws LogStorageException в случае внутренней ошибки
	 * @return колличество записей
	 */
	int countEntries(Collection<LogEntryMatcher> criterias) throws LogStorageException;
}
