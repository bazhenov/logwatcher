package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.spi.Matcher;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

import static com.farpost.logwatcher.storage.LuceneUtils.normalize;
import static org.apache.lucene.search.BooleanClause.Occur;

final public class LuceneMatcherMapperRules {

	@Matcher
	public Query date(DateMatcher matcher) {
		String lowerTerm = matcher.getDateFrom() != null
			? LuceneUtils.normilizeDate(matcher.getDateFrom())
			: "00000000";
		String upperTerm = matcher.getDateTo() != null
			? LuceneUtils.normilizeDate(matcher.getDateTo())
			: "99999999";
		return upperTerm.equals(lowerTerm)
			? new TermQuery(new Term("date", upperTerm))
			: new TermRangeQuery("date", lowerTerm, upperTerm, false, true);
	}

	@Matcher
	public Query severity(SeverityMatcher matcher) {
		BooleanQuery query = new BooleanQuery();
		Severity severity = matcher.getSeverity();

		for (Severity it : Severity.values()) {
			if (it.isEqualOrMoreImportantThan(severity)) {
				query.add(new TermQuery(new Term("severity", it.name())), Occur.SHOULD);
			}
		}

		return query;
	}

	@Matcher
	public Query checksum(ChecksumMatcher matcher) {
		return new TermQuery(new Term("checksum", normalize(matcher.getChecksum())));
	}

	@Matcher
	public Query attribute(AttributeValueMatcher matcher) {
		return new TermQuery(new Term("@" + matcher.getName(), matcher.getExpectedValue()));
	}

	@Matcher
	public Query contains(ContainsMatcher matcher) {
		return new WildcardQuery(new Term("message", "*" + normalize(matcher.getNeedle()) + "*"));
	}

	@Matcher
	public Query applicationId(ApplicationIdMatcher matcher) {
		return new TermQuery(new Term("applicationId", normalize(matcher.getApplicationId())));
	}
}