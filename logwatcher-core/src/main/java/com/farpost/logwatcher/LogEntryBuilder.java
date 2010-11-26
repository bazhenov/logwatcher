package com.farpost.logwatcher;

import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.timepoint.DateTime;

import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;

/**
 * Просто builder новых записей, которые записываются в хранилище.
 * <p/>
 * Пример использования:
 * <pre>
 * LogEntry enrty = LogEntryBuilder.newEntry().
 *   occuredAt(now()).
 *   saveIn(storage);
 * <p/>
 * // без записи в хранилище
 * LogEntry entry = LogEntryBuilder.newEntry().
 *   occuredAt(now()).
 *   create();
 * </pre>
 */
public class LogEntryBuilder {

	private DateTime time = now();
	private String group = "group";
	private String message = "message";
	private Severity severity = Severity.error;
	private String checksum = "2fde43";
	private Cause cause;
	private String applicationId = "some-application";
	private Map<String, String> attributes = new HashMap<String, String>();

	public LogEntry create() {
		return new LogEntryImpl(time, group, message, severity, checksum, applicationId, attributes, cause);
	}

	/**
	 * Устанавливает время когда произошло событие
	 *
	 * @param time время возникновения события
	 */
	public LogEntryBuilder occured(DateTime time) {
		this.time = time;
		return this;
	}

	/**
	 * Сохраняет запись в постояном хранилище и возвращает ссылку на нее.
	 *
	 * @param storage хранилище
	 * @return новая сконструированая запись
	 * @throws LogStorageException в случае ошибки во время сохранения хаписи
	 * @see LogStorage#writeEntry(LogEntry)
	 */
	public LogEntry saveIn(LogStorage storage) throws LogStorageException {
		LogEntry entry = create();
		storage.writeEntry(entry);
		return entry;
	}

	/**
	 * Записывает запись в постоянное хранилище.
	 * <p/>
	 * Этот метод идентичен методу {@link #saveIn(LogStorage)}, за тем лишь исключением,
	 * что он сохраняет запись в хранилище указанное количество раз. Очень удобно в целях тестирования.
	 *
	 * @param storage хранилище
	 * @param times	 сколько раз произвести запись
	 * @return запись
	 * @throws IllegalArgumentException в случае, если передано не положительное число указывающее количество
	 *                                  записей производимых в хранилище.
	 */
	public LogEntry saveMultipleTimesIn(LogStorage storage, int times) {
		if (times <= 0) {
			throw new IllegalArgumentException("Times argument should be positive integer");
		}
		LogEntry entry = create();
		for (int i = 0; i < times; i++) {
			storage.writeEntry(entry);
		}
		return entry;
	}

	public LogEntryBuilder checksum(String checksum) {
		this.checksum = checksum;
		return this;
	}

	public LogEntryBuilder applicationId(String applicationId) {
		this.applicationId = applicationId;
		return this;
	}

	public LogEntryBuilder severity(Severity severity) {
		this.severity = severity;
		return this;
	}

	public LogEntryBuilder attribute(String name, String value) {
		attributes.put(name, value);
		return this;
	}

	public LogEntryBuilder message(String message) {
		this.message = message;
		return this;
	}
}
