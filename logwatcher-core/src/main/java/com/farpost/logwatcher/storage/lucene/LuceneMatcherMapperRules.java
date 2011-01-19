package com.farpost.logwatcher.storage.lucene;

import com.farpost.logwatcher.storage.DateMatcher;
import com.farpost.logwatcher.storage.spi.Matcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import static com.farpost.logwatcher.storage.lucene.LuceneBdbLogStorage.normilizeDate;

public class LuceneMatcherMapperRules {

	@Matcher
	public Query date(DateMatcher matcher) {
		String lowerTerm = matcher.getDateFrom() != null
			? normilizeDate(matcher.getDateFrom())
			: "00000000";
		String upperTerm = matcher.getDateTo() != null
			? normilizeDate(matcher.getDateTo())
			: "99999999";
		return new TermRangeQuery("date", lowerTerm, upperTerm, true, true);
	}
}
