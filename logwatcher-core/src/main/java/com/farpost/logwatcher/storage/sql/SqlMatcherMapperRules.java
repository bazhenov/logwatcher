package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.storage.ApplicationIdMatcher;
import com.farpost.logwatcher.storage.ChecksumMatcher;
import com.farpost.logwatcher.storage.DateMatcher;
import com.farpost.logwatcher.storage.SeverityMatcher;
import com.farpost.timepoint.Date;

import static com.farpost.logwatcher.storage.sql.SqlLogStorage.date;

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
