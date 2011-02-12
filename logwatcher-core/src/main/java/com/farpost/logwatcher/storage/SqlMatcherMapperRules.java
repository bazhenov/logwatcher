package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.storage.spi.Matcher;
import com.farpost.timepoint.Date;

import static com.farpost.logwatcher.storage.SqlLogStorage.date;

public class SqlMatcherMapperRules {

	@Matcher
	public SqlWhereStatement onDate(DateMatcher matcher) {
		Date from = matcher.getDateFrom();
		Date to = matcher.getDateTo();
		return from.equals(to)
			? new SqlWhereStatement("l.date = ?", date(from))
			: new SqlWhereStatement("l.date > ? AND l.date <= ?", date(from), date(to));
	}

	@Matcher
	public SqlWhereStatement applicationId(ApplicationIdMatcher matcher) {
		return new SqlWhereStatement("l.application_id = ?", matcher.getApplicationId());
	}

	@Matcher
	public SqlWhereStatement checksum(ChecksumMatcher matcher) {
		return new SqlWhereStatement("l.checksum = ?", matcher.getChecksum());
	}

	@Matcher
	public SqlWhereStatement severity(SeverityMatcher matcher) {
		return new SqlWhereStatement("l.severity >= ?", matcher.getSeverity().getCode());
	}
}
