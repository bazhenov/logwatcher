package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.Visitor;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LogEntriesFinder {

	private final List<LogEntryMatcher> criteria = new LinkedList<>();
	private int limit = 1000;

	public LogEntriesFinder date(LocalDate date) {
		criteria.add(new DateMatcher(date));
		return this;
	}

	/**
	 * Добавляет критерий поиска по диапазону дат.
	 *
	 * @param from начало диапазона (исключая саму дату)
	 * @param to	 конец диапазона (включительно)
	 * @return this
	 */
	public LogEntriesFinder date(LocalDate from, LocalDate to) {
		criteria.add(new DateMatcher(from, to));
		return this;
	}

	/**
	 * Добавляет критерий по идентификатору приложения
	 *
	 * @param applicationId идентификатор приложения
	 * @return this
	 */
	public LogEntriesFinder applicationId(String applicationId) {
		return withCriterion(new ApplicationIdMatcher(applicationId));
	}

	/**
	 * Добавляет критерий по контрольной сумме
	 *
	 * @param checksum контрольная сумма
	 * @return this
	 */
	public LogEntriesFinder checksum(String checksum) {
		return withCriterion(new ChecksumMatcher(checksum));
	}

	public LogEntriesFinder severity(Severity severity) {
		return withCriterion(new SeverityMatcher(severity));
	}

	public LogEntriesFinder contains(String part) {
		return withCriterion(new ContainsMatcher(part));
	}

	public LogEntriesFinder causedBy(String type) {
		return withCriterion(new CauseTypeMatcher(type));
	}

	public LogEntriesFinder attribute(String name, String expectedValue) {
		return withCriterion(new AttributeValueMatcher(name, expectedValue));
	}

	/**
	 * @param limit ограничение на количество записей, обрабатываемых методами {@link #find(LogStorage)} и
	 * {@link #walk(LogStorage, Visitor)}.
	 * @return this
	 */
	public LogEntriesFinder limit(int limit) {
		this.limit = limit;
		return this;
	}

	private LogEntriesFinder withCriterion(LogEntryMatcher matcher) {
		criteria.add(matcher);
		return this;
	}

	public LogEntriesFinder withCriterion(Collection<LogEntryMatcher> matchers) {
		criteria.addAll(matchers);
		return this;
	}

	/**
	 * Возвращает коллекцию matcher'ов по заданным finder'ом критериям.
	 *
	 * @return коллекция matcher'ов
	 */
	public List<LogEntryMatcher> getCriteria() {
		return criteria;
	}

	/**
	 * Возвращает количество записей в хранилище подпадающих под заданные критерии.
	 *
	 * @param storage Хранилище логов
	 * @return количество записей
	 * @throws LogStorageException в случае внутренней ошибки
	 * @throws InvalidCriteriaException в случае некорректно составленных критериев
	 */
	public int count(LogStorage storage) throws LogStorageException, InvalidCriteriaException {
		return storage.countEntries(criteria);
	}

	/**
	 * Возвращает список записей удовлетворяющих заданным критериям
	 *
	 * @param storage хранилище
	 * @return список записей
	 * @throws InvalidCriteriaException в случае если неверно заданы критерии фильтрации
	 * @throws LogStorageException      в случае внутренней ошибки хранилища
	 * @see LogStorage#findEntries(Collection, int)
	 */
	public List<LogEntry> find(LogStorage storage) throws LogStorageException,
		InvalidCriteriaException {
		return storage.findEntries(criteria, limit);
	}

	public <T> T walk(LogStorage storage, Visitor<LogEntry, T> visitor)
		throws LogStorageException, InvalidCriteriaException {

		return storage.walk(criteria, limit, visitor);
	}

	public List<LogEntryMatcher> all() {
		return new ArrayList<>();
	}
}
