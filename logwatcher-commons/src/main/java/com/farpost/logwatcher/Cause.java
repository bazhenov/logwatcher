package com.farpost.logwatcher;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import static com.farpost.logwatcher.StackTraceFormatter.extractStackTrace;

/**
 * Причина ошибки. Запись лога {@link LogEntry} может иметь причину. Как правило - это
 * исключительная ситуация которая привела к ошибке.
 * <p/>
 * Причина, как и исключительная ситуация, описывается типом, сообщением и текстом (stacktrace'ом)
 */
@XmlType
public class Cause {

	@XmlAttribute
	private String type;

	@XmlElement
	private String message;

	@XmlElement
	private String stackTrace;

	@XmlElement
	private Cause cause;

	/**
	 * Этот конструктор не предназначен для прямого ипользования. Нужен для корректной работы JAXB.
	 */
	@Deprecated
	public Cause() {
		this(null, null, null);
	}

	public Cause(String type, String message, String stackTrace) {
		this.type = type;
		this.message = message;
		this.stackTrace = stackTrace;
	}

	public Cause(String type, String message, String stackTrace, Cause cause) {
		this(type, message, stackTrace);
		this.cause = cause;
	}

	public Cause(Throwable throwable) {
		type = throwable.getClass().getName();
		message = throwable.getMessage();
		stackTrace = extractStackTrace(throwable);
		cause = throwable.getCause() == null
			? null
			: new Cause(throwable.getCause());
	}

	public String getType() {
		return type;
	}

	public Cause getCause() {
		return cause;
	}

	public Cause getRootCause() {
		Cause cause = getCause();
		return cause == null
			? this
			: cause.getRootCause();
	}

	public String getMessage() {
		return message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Cause cause1 = (Cause) o;

		if (cause != null
			? !cause.equals(cause1.cause)
			: cause1.cause != null) {
			return false;
		}
		if (message != null
			? !message.equals(cause1.message)
			: cause1.message != null) {
			return false;
		}
		if (stackTrace != null
			? !stackTrace.equals(cause1.stackTrace)
			: cause1.stackTrace != null) {
			return false;
		}
		if (type != null
			? !type.equals(cause1.type)
			: cause1.type != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = type != null
			? type.hashCode()
			: 0;
		result = 31 * result + (message != null
			? message.hashCode()
			: 0);
		result = 31 * result + (stackTrace != null
			? stackTrace.hashCode()
			: 0);
		result = 31 * result + (cause != null
			? cause.hashCode()
			: 0);
		return result;
	}
}
