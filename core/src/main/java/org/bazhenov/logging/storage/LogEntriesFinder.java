package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;

import java.util.*;

import org.bazhenov.logging.AggregatedLogEntry;

public class LogEntriesFinder {

	private final LogStorage storage;
	private final List<LogEntryMatcher> criterias = new LinkedList<LogEntryMatcher>();
	@Deprecated
	private Date date;

	public LogEntriesFinder(LogStorage storage) {
		this.storage = storage;
	}

	public LogEntriesFinder date(Date date) {
		this.date = date;
		criterias.add(new DateMatcher(date));
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
	 */
	public AggregatedLogEntry findFirst() throws LogStorageException {
		List<AggregatedLogEntry> entries = storage.getEntries(date);
		return entries.size() > 0
			? entries.get(0)
			: null;
	}

	/**
	 * Возвращает количество записей в хранилище подпадающих под заданные критерии.
	 * @return количество записей
	 * @throws LogStorageException в случае внутренней ошибки
	 */
	public int count() throws LogStorageException, InvalidCriteriaException {
		return storage.countEntries(criterias);
	}
}
