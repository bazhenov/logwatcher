package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

/**
 * Обьект представляющий собой запись лога. Запись лога описывается датой, группой, описанием,
 * важностью (severity), контрольной суммой и причиной (cause).
 * <p/>
 * Severity описывается соответствуюшим enum'ом и необходима для определения серьезности ошибки.
 * <p/>
 * Контрольная сумма нужна для группировки нескольких записей лога в одну. Например, один и тот же
 * exception должен иметь одинаковую контрольную сумму для того чтобы можно было сгруппировать
 * исключительные ситуаци для получения статистической информации.
 */
public class LogEntry {
	private DateTime date;
	private String message;
	private Severity severity;
	private String group;
	private String checksum;
	private String application;
	private Cause cause;

	public LogEntry(DateTime date, String group, String message, Severity severity, String checksum, String application) {
		this.date = date;
		this.group = group;
		this.message = message;
		this.severity = severity;
		this.checksum = checksum;
		this.application = application;
	}

	public LogEntry(DateTime date, String group, String message, Severity severity, String checksum,
	                Cause cause, String application) {
		this(date, group, message, severity, checksum, application);
		this.cause = cause;
	}


	public DateTime getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getGroup() {
		return group;
	}

	public Cause getCause() {
		return cause;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getApplication() {
		return application;
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		LogEntry entry = (LogEntry) o;

		if ( cause != null
			? !cause.equals(entry.cause)
			: entry.cause != null ) {
			return false;
		}
		if ( checksum != null
			? !checksum.equals(entry.checksum)
			: entry.checksum != null ) {
			return false;
		}
		if ( date != null
			? !date.equals(entry.date)
			: entry.date != null ) {
			return false;
		}
		if ( group != null
			? !group.equals(entry.group)
			: entry.group != null ) {
			return false;
		}
		if ( message != null
			? !message.equals(entry.message)
			: entry.message != null ) {
			return false;
		}
		if ( application != null
			? !application.equals(entry.application)
			: entry.application != null ) {
			return false;
		}
		if ( severity != entry.severity ) {
			return false;
		}

		return true;
	}
}
