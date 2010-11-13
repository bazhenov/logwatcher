package com.farpost.logwatcher.storage;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;

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
 *
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
	 * @throws com.farpost.logwatcher.storage.LogStorageException в случае ошибки во время сохранения хаписи
	 * @see LogStorage#writeEntry(LogEntry)
	 */
	public LogEntry saveIn(LogStorage storage) throws LogStorageException {
		LogEntry entry = create();
		storage.writeEntry(entry);
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
