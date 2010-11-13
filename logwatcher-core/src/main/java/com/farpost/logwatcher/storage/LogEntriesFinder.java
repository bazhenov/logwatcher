package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Visitor;
import com.farpost.timepoint.Date;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LogEntriesFinder {

	private final List<LogEntryMatcher> criterias = new LinkedList<LogEntryMatcher>();

	public LogEntriesFinder date(Date date) {
		criterias.add(new DateMatcher(date));
		return this;
	}

	/**
	 * Добавляет критерий поиска по диапазону дат.
	 *
	 * @param from начало диапазона (исключая саму дату)
	 * @param to   конец диапазона (включительно)
	 */
	public LogEntriesFinder date(Date from, Date to) {
		criterias.add(new DateMatcher(from, to));
		return this;
	}

	/**
	 * Добавляет критерий по идентификатору приложения
	 *
	 * @param applicationId идентификатор приложения
	 */
	public LogEntriesFinder applicationId(String applicationId) {
		return withCriteria(new ApplicationIdMatcher(applicationId));
	}

	/**
	 * Добавляет критерий по контрольной сумме
	 *
	 * @param checksum контрольная сумма
	 */
	public LogEntriesFinder checksum(String checksum) {
		return withCriteria(new ChecksumMatcher(checksum));
	}

	public LogEntriesFinder severity(Severity severity) {
		return withCriteria(new SeverityMatcher(severity));
	}

	public LogEntriesFinder contains(String part) {
		return withCriteria(new ContainsMatcher(part));
	}

	private LogEntriesFinder withCriteria(LogEntryMatcher matcher) {
		criterias.add(matcher);
		return this;
	}

	public LogEntriesFinder withCriteria(Collection<LogEntryMatcher> matchers) {
		criterias.addAll(matchers);
		return this;
	}

	/**
	 * Возвращает первую запись с данными условиями отбора или {@code null}, если таких записей не
	 * существует.
	 * <p/>
	 * Данный метод не дает никаких гарантий в отношении порядка извлекаемых записей.
	 *
	 * @return первая запись подпадающиая под условия
	 * @throws LogStorageException в случае возникновения внутренней ошибки  @param storage
	 */
	public AggregatedEntry findFirst(LogStorage storage)
		throws LogStorageException, InvalidCriteriaException {
		List<AggregatedEntry> entries = findAggregated(storage);
		return entries.size() > 0
			? entries.get(0)
			: null;
	}

	/**
	 * Возвращает коллекцию matcher'ов по заданным finder'ом критериям.
	 *
	 * @return коллекция matcher'ов
	 */
	public List<LogEntryMatcher> criterias() {
		return criterias;
	}

	/**
	 * Возвращает количество записей в хранилище подпадающих под заданные критерии.
	 *
	 * @return количество записей
	 * @throws LogStorageException в случае внутренней ошибки
	 */
	public int count(LogStorage storage) throws LogStorageException, InvalidCriteriaException {
		return storage.countEntries(criterias);
	}

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям
	 *
	 * @param storage хранилище
	 * @return список записей
	 * @throws InvalidCriteriaException в случае если неверно заданы критерии фильтрации
	 * @throws LogStorageException в случае внутренней ошибки хранилища
	 * @see LogStorage#findEntries(Collection)
	 */
	public List<LogEntry> find(LogStorage storage) throws LogStorageException,
		InvalidCriteriaException {
		return storage.findEntries(criterias);
	}

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям сгруппированных по контрольной
	 * сумме.
	 *
	 * @param storage хранилище
	 * @return список аггрегированных записей
	 * @throws InvalidCriteriaException в случае если неверно заданы критерии фильтрации
	 * @throws LogStorageException в случае внутренней ошибки хранилища
	 * @see LogStorage#findAggregatedEntries(Collection)
	 */
	public List<AggregatedEntry> findAggregated(LogStorage storage)
		throws LogStorageException, InvalidCriteriaException {
		return storage.findAggregatedEntries(criterias);
	}

	public void walk(LogStorage storage, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {

		storage.walk(criterias, visitor);
	}

	public List<LogEntryMatcher> all() {
		return new ArrayList<LogEntryMatcher>();
	}
}
