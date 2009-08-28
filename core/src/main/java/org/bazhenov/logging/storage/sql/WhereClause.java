package org.bazhenov.logging.storage.sql;

import static java.util.Collections.addAll;
import java.util.List;

/**
 * Вспомогательный класс для формирования WHERE выражения sql запроса. Предоставляет
 * очень простой родовой интерфейс
 * <pre>
 * WhereClause where = new WhereClause();
 *
 * where.
 *   and("date = ?", new Date()).
 *   and("count > ?", 5);
 * </pre>
 */
public class WhereClause {

	private final List<Object> arguments;
	private final StringBuilder buffer;

	public WhereClause(StringBuilder buffer, List arguments) {
		this.buffer = buffer;
		this.arguments = arguments;
	}

	public WhereClause and(String criteria, Object... args) {
		if ( buffer.length() > 0 ) {
			buffer.append(" AND ");
		}
		buffer.append(criteria);
		addAll(arguments, args);
		return this;
	}
}
