package com.farpost.logwatcher;

import com.farpost.logwatcher.AnnotationDrivenQueryTranslator;
import com.farpost.logwatcher.InvalidQueryException;
import com.farpost.logwatcher.QueryTranslator;
import com.farpost.logwatcher.TranslationRulesImpl;
import com.farpost.logwatcher.storage.*;
import com.farpost.timepoint.Date;
import org.bazhenov.logging.Severity;
import org.testng.annotations.Test;

import static com.farpost.timepoint.Date.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class QueryTranslatorTest {

	QueryTranslator translator = new AnnotationDrivenQueryTranslator(new TranslationRulesImpl());

	@Test
	public void severityCriteria() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new SeverityMatcher(Severity.warning);

		assertThat(translate("severity: warning"), equalTo(expectedMatcher));
	}

	@Test
	public void applicationCriteria() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new ApplicationIdMatcher("frontend");

		assertThat(translate("at: frontend"), equalTo(expectedMatcher));
	}

	@Test
	public void dateCriteria() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new DateMatcher(today());

		assertThat(translate("occurred: " + today()), equalTo(expectedMatcher));
	}

	@Test
	public void dateIntervalCriteria() throws InvalidQueryException {
		Date date = today();
		LogEntryMatcher expectedMatcher = new DateMatcher(date.minusDay(1), date);

		assertThat(translate("occurred: " + date+" / "+date.minusDay(1)), equalTo(expectedMatcher));
	}

	@Test
	public void dateHumanReadableIntervalCriteria() throws InvalidQueryException {
		Date today = today();
		LogEntryMatcher expectedMatcher = new DateMatcher(today.minusDay(2), today);

		assertThat(translate("occurred: last 2 days"), equalTo(expectedMatcher));
	}

	@Test
	public void attributeValues() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new AttributeValueMatcher("machine", "aux2");

		assertThat(translate("@machine: aux2"), equalTo(expectedMatcher));
	}

	private LogEntryMatcher translate(String query) throws InvalidQueryException {
		LogEntryMatcher matcher = translator.translate(query).get(0);
		if ( matcher == null ) {
			throw new AssertionError("No matcher created for: " + query);
		}
		return matcher;
	}
}
