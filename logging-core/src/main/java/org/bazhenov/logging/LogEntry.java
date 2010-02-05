package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

import java.util.*;

/**
 * Обьект представляющий собой запись лога. Запись лога описывается датой, группой, описанием,
 * важностью ({@link Severity}), контрольной суммой и причиной ({@link Cause}).
 * <p/>
 * Severity описывается соответствуюшим enum'ом и необходима для определения серьезности ошибки.
 * <p/>
 * Контрольная сумма нужна для группировки нескольких записей лога в одну. Например, один и тот же
 * exception должен иметь одинаковую контрольную сумму для того чтобы можно было сгруппировать
 * исключительные ситуации для получения статистической информации.
 */
public class LogEntry {

	private final DateTime date;
	private final String message;
	private final Severity severity;
	private final String group;
	private volatile String checksum;
	private final String applicationId;
	private final Map<String, String> attributes;
	private final Cause cause;

	public LogEntry(DateTime date, String group, String message, Severity severity, String checksum,
	                String applicationId, Map<String, String> attributes) {
		this(date, group, message, severity, checksum, applicationId, attributes, null);
	}

	public LogEntry(DateTime date, String group, String message, Severity severity, String checksum,
	                String applicationId, Map<String, String> attributes, Cause cause) {
		this.date = date;
		this.group = group;
		this.message = message;
		this.severity = severity;
		this.checksum = checksum;
		this.applicationId = applicationId;
		this.cause = cause;
		this.attributes = attributes == null
			? new HashMap<String, String>()
			: new HashMap<String, String>(attributes);
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

	public String getCategory() {
		return group;
	}

	public Cause getCause() {
		return cause;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		LogEntry logEntry = (LogEntry) o;

		if ( applicationId != null
			? !applicationId.equals(logEntry.applicationId)
			: logEntry.applicationId != null ) {
			return false;
		}
		if ( attributes != null
			? !attributes.equals(logEntry.attributes)
			: logEntry.attributes != null ) {
			return false;
		}
		if ( cause != null
			? !cause.equals(logEntry.cause)
			: logEntry.cause != null ) {
			return false;
		}
		if ( checksum != null
			? !checksum.equals(logEntry.checksum)
			: logEntry.checksum != null ) {
			return false;
		}
		if ( date != null
			? !date.equals(logEntry.date)
			: logEntry.date != null ) {
			return false;
		}
		if ( group != null
			? !group.equals(logEntry.group)
			: logEntry.group != null ) {
			return false;
		}
		if ( message != null
			? !message.equals(logEntry.message)
			: logEntry.message != null ) {
			return false;
		}
		if ( severity != logEntry.severity ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = date != null
			? date.hashCode()
			: 0;
		result = 31 * result + (message != null
			? message.hashCode()
			: 0);
		result = 31 * result + (severity != null
			? severity.hashCode()
			: 0);
		result = 31 * result + (group != null
			? group.hashCode()
			: 0);
		result = 31 * result + (checksum != null
			? checksum.hashCode()
			: 0);
		result = 31 * result + (applicationId != null
			? applicationId.hashCode()
			: 0);
		result = 31 * result + (attributes != null
			? attributes.hashCode()
			: 0);
		result = 31 * result + (cause != null
			? cause.hashCode()
			: 0);
		return result;
	}
}
