package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.*;

import java.util.*;

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
	void removeEntries(String checksum) throws LogStorageException;

	/**
	 * Создает синоним для контрольной суммы. Все записи с первой переданной контрольной суммой будут
	 * превращены в записи со второй контрольной суммой. Так же имплементация обязана предоставлять
	 * гарантию сохранения этого маппинга в будущем. Другими словами, если позже приходят записи с изначальной
	 * контрольной суммой, им автоматически должен назначатся alias контрольной суммы.
	 *
	 * @param checksum контролная сумма, которую надо смапить
	 * @param alias		 контрольная сумма - синоним
	 */
	void createChecksumAlias(String checksum, String alias) throws LogStorageException;

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям.
	 *
	 * @param criterias критерии отбора записей
	 * @return список записей
	 * @throws LogStorageException			в случае внутренне ошибки хранилища
	 * @throws InvalidCriteriaException в случае если указанные неверные критерии отбора
	 */
	List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям сгруппированных по контрольной
	 * сумме
	 *
	 * @param criterias критерии отбора записей
	 * @return список записей
	 * @throws LogStorageException			в случае внутренне ошибки хранилища
	 * @throws InvalidCriteriaException в случае если указанные неверные критерии отбора
	 */
	List<AggregatedEntry> findAggregatedEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Данный метод проходит по записям {@link LogEntry} передавая каждую в заданный visitor. Visitor
	 * не должен менять передаваемые ему записи. Имплементации этих методов не дают никаких гаранитий
	 * относительно того в каком потоке будет вызыватся vistor, поэтому имплементация visitor'а должна
	 * быть потокобезопасна.
	 *
	 * @param criterias критерии по которым осуществляется итерация
	 * @param visitor	 visitor
	 * @throws LogStorageException			в случае внутренней ошибки хранилища
	 * @throws InvalidCriteriaException в случае если некорректно заданы критерии поиска
	 */
	void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException;

	/**
	 * Возвращает список аггрегированных записей за указанную дату с указанным severity.
	 *
	 * @param date		 дата
	 * @param severity уровень
	 * @return список аггрегированных записей
	 * @throws LogStorageException в случае возникновения внутренних ошибок хранилища
	 */
	@Deprecated
	List<AggregatedEntry> getAggregatedEntries(Date date, Severity severity)
		throws LogStorageException;

	/**
	 * Возвращает список аггрегированных записей за указанную дату с указанным severity произошедших
	 * в указанном приложении.
	 *
	 * @param applicationId идентификатор приложения
	 * @param date					дата
	 * @param severity			уровень
	 * @return список аггрегированных записей
	 * @throws LogStorageException в случае возникновения внутренних ошибок хранилища
	 */
	List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date, Severity severity)
		throws LogStorageException;
}
