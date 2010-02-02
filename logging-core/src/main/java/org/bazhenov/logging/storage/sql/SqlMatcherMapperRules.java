package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.storage.DateMatcher;
import org.bazhenov.logging.storage.ApplicationIdMatcher;
import org.bazhenov.logging.storage.ChecksumMatcher;
import org.bazhenov.logging.storage.SeverityMatcher;
import static org.bazhenov.logging.storage.sql.SqlLogStorage.date;
import com.farpost.timepoint.Date;

public class SqlMatcherMapperRules {

	@Matcher
	public void onDate(DateMatcher matcher, WhereClause where) {
		Date from = matcher.getDateFrom();
		Date to = matcher.getDateTo();
		if ( from.equals(to) ) {
			where.and("l.date = ?", date(from));
		}else{
			where.and("l.date > ? AND l.date <= ?", date(from), date(to));
		}
	}

	@Matcher
	public void applicationId(ApplicationIdMatcher matcher, WhereClause where) {
		where.and("l.application_id = ?", matcher.getApplicationId());
	}

	@Matcher
	public void checksum(ChecksumMatcher matcher, WhereClause where) {
		where.and("l.checksum = ?", matcher.getChecksum());
	}

	@Matcher
	public void severity(SeverityMatcher matcher, WhereClause where) {
		where.and("l.severity >= ?", matcher.getSeverity().getCode());
	}
}
