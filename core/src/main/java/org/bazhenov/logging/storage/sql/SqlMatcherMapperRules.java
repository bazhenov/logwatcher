package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.storage.DateMatcher;
import org.bazhenov.logging.storage.ApplicationIdMatcher;
import org.bazhenov.logging.storage.ChecksumMatcher;
import static org.bazhenov.logging.storage.sql.SqlLogStorage.date;

public class SqlMatcherMapperRules {

	@Matcher
	public void onDate(DateMatcher matcher, WhereClause where) {
		where.and("l.date = ?", date(matcher.getDate()));
	}

	@Matcher
	public void applicationId(ApplicationIdMatcher matcher, WhereClause where) {
		where.and("l.application_id = ?", matcher.getApplicationId());
	}

	@Matcher
	public void checksum(ChecksumMatcher matcher, WhereClause where) {
		where.and("l.checksum = ?", matcher.getChecksum());
	}
}
