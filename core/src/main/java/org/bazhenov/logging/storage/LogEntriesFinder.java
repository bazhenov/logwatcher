package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;

import java.util.*;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.Severity;

public class LogEntriesFinder {

	private final List<LogEntryMatcher> criterias = new LinkedList<LogEntryMatcher>();

	public LogEntriesFinder date(Date date) {
		criterias.add(new DateMatcher(date));
		return this;
	}

	/**
	 * Добавляет критерий поиска по диапазону дат.
	 * @param from начало диапазона (исключая саму дату)
	 * @param to конец диапазона (включительно)
	 */
	public LogEntriesFinder date(Date from, Date to) {
		criterias.add(new DateMatcher(from, to));
		return this;
	}

	/**
	 * Добавляет критерий по идентификатору приложения
	 * @param applicationId идентификатор приложения
	 */
	public LogEntriesFinder applicationId(String applicationId) {
		return withCriteria(new ApplicationIdMatcher(applicationId));
	}

	/**
	 * Добавляет критерий по контрольной сумме
	 * @param checksum контрольная сумма
	 */
	public LogEntriesFinder checksum(String checksum) {
		return withCriteria(new ChecksumMatcher(checksum));
	}

	public LogEntriesFinder severity(Severity severity) {
		return withCriteria(new SeverityMatcher(severity));
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
	 * Возвращает первую запись с данными условиями отбора или {@code null}, если таких записей
	 * не существует.
	 * <p />
	 * Данный метод не дает никаких гарантий в отношении порядка извлекаемых записей.
	 *
	 * @return первая запись подпадающиая под условия
	 * @throws LogStorageException в случае возникновения внутренней ошибки
	 * @param storage
	 */
	public AggregatedLogEntry findFirst(LogStorage storage) throws LogStorageException,
		InvalidCriteriaException {
		List<AggregatedLogEntry> entries = find(storage);
		return entries.size() > 0
			? entries.get(0)
			: null;
	}

	/**
	 * Возвращает количество записей в хранилище подпадающих под заданные критерии.
	 * @return количество записей
	 * @throws LogStorageException в случае внутренней ошибки
	 * @param storage
	 */
	public int count(LogStorage storage) throws LogStorageException, InvalidCriteriaException {
		return storage.countEntries(criterias);
	}

	public List<AggregatedLogEntry> find(LogStorage storage) throws LogStorageException, InvalidCriteriaException {
		return storage.findEntries(criterias);
	}
}
