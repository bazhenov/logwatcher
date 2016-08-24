package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Visitor;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.List;

/**
 * Имплементации этого интерфейса сохраняют обьекты типа LogEntry в постоянном хранилище.
 */
public interface LogStorage {

	/**
	 * Сохраняет обьект {@link LogEntry} в постоянном хранилище.
	 * <p/>
	 * Учтите что сохранение не влияет на обьект LogEntry. Ему не выдается никакого идентификационного
	 * номера. Если вы передадите один и тот же LogEntry в этот метод два раза, то произойдет две
	 * записи в постоянное хранилище.
	 *
	 * @param entry запись лога
	 * @throws LogStorageException в случае внутренней ошибки
	 */
	void writeEntry(LogEntry entry) throws LogStorageException;

	/**
	 * Удаляет все неагрегированные записи их хранилища с датой меньше указанной.
	 * <p/>
	 * Данный метод используется для периодической очистки хранилища от старых
	 * записей
	 *
	 * @param date дата начиная с которой записи будут оставлены
	 * @return возвращает количество удаленных записей
	 * @throws LogStorageException в случае возникновения внутренних ошибок хранилища
	 */
	int removeOldEntries(LocalDate date) throws LogStorageException;

	/**
	 * Подсчитывает колличество записей в хранилище с заданными условиями.
	 *
	 * @param criteria условия отбора записей или {@code null} если интересует общее количество
	 *                 записей в хранилище
	 * @return колличество записей
	 * @throws LogStorageException      в случае внутренней ошибки
	 * @throws InvalidCriteriaException в случае если заданные критерии неверны
	 */
	int countEntries(Collection<LogEntryMatcher> criteria)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Удаляет из хранилища записи с указанной контрольной суммой
	 *
	 * @param checksum контрольная сумма
	 * @throws LogStorageException в случае внутренней ошибки
	 */
	void removeEntriesWithChecksum(String checksum) throws LogStorageException;

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям.
	 *
	 * @param criteria критерии отбора записей
	 * @param limit ограничение на количество возвращаемых записей
	 * @return список записей
	 * @throws LogStorageException      в случае внутренней ошибки хранилища
	 * @throws InvalidCriteriaException в случае если указанные неверные критерии отбора
	 */
	List<LogEntry> findEntries(Collection<LogEntryMatcher> criteria, int limit)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Данный метод проходит по записям {@link LogEntry} передавая каждую в заданный visitor. Visitor
	 * не должен менять передаваемые ему записи. Имплементации этих методов не дают никаких гаранитий
	 * относительно того в каком потоке будет вызыватся visitor, поэтому имплементация visitor'а должна
	 * быть потокобезопасна.
	 *
	 * @param criteria критерии по которым осуществляется итерация
	 * @param limit ограничение на количество обрабатываемых записей
	 * @param visitor  visitor  @return результат расчитанный имплементацией {@link Visitor}.
	 * @throws LogStorageException      в случае внутренней ошибки хранилища
	 * @throws InvalidCriteriaException в случае если некорректно заданы критерии поиска
	 */
	void walk(Collection<LogEntryMatcher> criteria, int limit, Visitor<LogEntry, ?> visitor)
		throws LogStorageException, InvalidCriteriaException;

}
