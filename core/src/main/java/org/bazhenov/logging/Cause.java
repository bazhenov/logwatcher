package org.bazhenov.logging;

/**
 * Причина ошибки. Запись лога {@link LogEntry} может иметь причину. Как правило - это
 * исключительная ситуация которая привела к ошибке.
 * <p/>
 * Причина, как и исключительная ситуация, описывается типом, сообщением и текстом (stacktrace'ом)
 */
public class Cause {

	private String type;
	private String message;
	private String stackTrace;
	private Cause cause;

	public Cause(String type, String message, String stackTrace) {
		this.type = type;
		this.message = message;
		this.stackTrace = stackTrace;
	}

	public Cause(String type, String message, String stackTrace, Cause cause) {
		this(type, message, stackTrace);
		this.cause = cause;
	}

	public String getType() {
		return type;
	}

	public Cause getCause() {
		return cause;
	}

	public String getMessage() {
		return message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Cause cause1 = (Cause) o;

		if ( cause != null
			? !cause.equals(cause1.cause)
			: cause1.cause != null ) {
			return false;
		}
		if ( message != null
			? !message.equals(cause1.message)
			: cause1.message != null ) {
			return false;
		}
		if ( stackTrace != null
			? !stackTrace.equals(cause1.stackTrace)
			: cause1.stackTrace != null ) {
			return false;
		}
		if ( type != null
			? !type.equals(cause1.type)
			: cause1.type != null ) {
			return false;
		}

		return true;
	}
}
