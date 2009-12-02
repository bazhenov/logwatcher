package org.bazhenov.logging;

import java.lang.reflect.InvocationTargetException;

/**
 * Эта исключительная ситуация генерируется объектами типа {@link QueryParser} в случае
 * если запрос в некорректном формате.
 */
public class InvalidQueryException extends Exception {

	public InvalidQueryException(String message) {
		super(message);
	}

	public InvalidQueryException(Exception cause) {
		super(cause);
	}
}
