package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.spi.Matcher;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

import static com.farpost.logwatcher.storage.LuceneUtils.normalize;
import static com.farpost.logwatcher.storage.LuceneUtils.normalizeDate;
import static org.apache.lucene.search.BooleanClause.Occur;
import static org.apache.lucene.search.NumericRangeQuery.newLongRange;

final public class LuceneMatcherMapperRules {

	@Matcher
	public Query date(DateMatcher matcher) {
		long lowerTerm = matcher.getDateFrom() != null
			? normalizeDate(matcher.getDateFrom())
			: 0;
		long upperTerm = matcher.getDateTo() != null
			? normalizeDate(matcher.getDateTo())
			: Long.MAX_VALUE;

		return newLongRange("date", lowerTerm, upperTerm, true, false);
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
		return new TermQuery(new Term("@" + matcher.getName(), normalize(matcher.getExpectedValue())));
	}

	@Matcher
	public Query causeType(CauseTypeMatcher matcher) {
		return new TermQuery(new Term("caused-by", normalize(matcher.getExpectedType())));
	}

	@Matcher
	public Query contains(ContainsMatcher matcher) throws ParseException {
		return new MultiFieldQueryParser(Version.LUCENE_30, new String[]{"message", "stacktrace"},
			new StandardAnalyzer(Version.LUCENE_30)).parse(matcher.getNeedle());
	}

	@Matcher
	public Query applicationId(ApplicationIdMatcher matcher) {
		return new TermQuery(new Term("applicationId", normalize(matcher.getApplicationId())));
	}
}
