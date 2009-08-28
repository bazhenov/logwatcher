package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.storage.DateMatcher;
import static org.bazhenov.logging.storage.sql.MySqlLogStorage.date;

public class SqlMatcherMapperRules {

	@Matcher
	public void onDate(DateMatcher matcher, WhereClause where) {
		where.and("l.date = ?", date(matcher.getDate()));
	}
}
