package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.Visitor;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	 * @param criterias условия отбора записей или {@code null} если интересует общее количество
	 *                  записей в хранилище
	 * @return колличество записей
	 * @throws LogStorageException			в случае внутренней ошибки
	 * @throws InvalidCriteriaException в случае если заданные критерии неверны
	 */
	int countEntries(Collection<LogEntryMatcher> criterias)
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
	 * @param criterias критерии отбора записей
	 * @return список записей
	 * @throws LogStorageException			в случае внутренней ошибки хранилища
	 * @throws InvalidCriteriaException в случае если указанные неверные критерии отбора
	 */
	List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Данный метод проходит по записям {@link LogEntry} передавая каждую в заданный visitor. Visitor
	 * не должен менять передаваемые ему записи. Имплементации этих методов не дают никаких гаранитий
	 * относительно того в каком потоке будет вызыватся visitor, поэтому имплементация visitor'а должна
	 * быть потокобезопасна.
	 *
	 * @param criterias критерии по которым осуществляется итерация
	 * @param visitor	 visitor
	 * @return результат расчитанный имплементацией {@link Visitor}.
	 * @throws LogStorageException			в случае внутренней ошибки хранилища
	 * @throws InvalidCriteriaException в случае если некорректно заданы критерии поиска
	 */
	<T> T walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry, T> visitor)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Возвращает множество applicationId для которых в хранилище существуют записи за указанную дату
	 *
	 * @param date дата выборки
	 * @return множество идентификаторов приложений
	 */
	Set<String> getUniquieApplicationIds(LocalDate date);

	/**
	 * Возвращает список аггрегированных записей за указанную дату с указанным severity произошедших
	 * в указанном приложении.
	 *
	 * @param applicationId идентификатор приложения
	 * @param date					дата
	 * @param severity			уровень
	 * @return список аггрегированных записей
	 * @throws LogStorageException			в случае возникновения внутренних ошибок хранилища
	 * @throws InvalidCriteriaException в случае задания клиентом некорректных критериев отбора
	 */
	List<AggregatedEntry> getAggregatedEntries(String applicationId, LocalDate date, Severity severity)
		throws LogStorageException, InvalidCriteriaException;
}
