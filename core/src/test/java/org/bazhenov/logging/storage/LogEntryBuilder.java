package org.bazhenov.logging.storage;

import org.bazhenov.logging.*;
import com.farpost.timepoint.DateTime;
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

	public LogEntry create() {
		return new LogEntry(time, group, message, severity, checksum, cause, applicationId);
	}

	/**
	 * Устанавливает время когда произошло событие
	 * @param time время возникновения события
	 */
	public LogEntryBuilder occured(DateTime time) {
		this.time = time;
		return this;
	}

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
}