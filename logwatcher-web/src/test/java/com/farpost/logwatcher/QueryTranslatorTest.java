package com.farpost.logwatcher;

import com.farpost.logwatcher.storage.*;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class QueryTranslatorTest {

	private static final LocalDate today = LocalDate.now();

	private QueryTranslator translator = new AnnotationDrivenQueryTranslator(new TranslationRulesImpl());

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
		LogEntryMatcher expectedMatcher = new DateMatcher(today);

		assertThat(translate("occurred: " + today), equalTo(expectedMatcher));
	}

	@Test
	public void dateIntervalCriteria() throws InvalidQueryException {
		LocalDate date = today;
		LogEntryMatcher expectedMatcher = new DateMatcher(date.minusDays(1), date);

		assertThat(translate("occurred: " + date + " / " + date.minusDays(1)), equalTo(expectedMatcher));
	}

	@Test
	public void dateHumanReadableIntervalCriteria() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new DateMatcher(today.minusDays(2), today);

		assertThat(translate("occurred: last 2 days"), equalTo(expectedMatcher));
	}

	@Test
	public void attributeValues() throws InvalidQueryException {
		LogEntryMatcher expectedMatcher = new AttributeValueMatcher("machine", "aux2");

		assertThat(translate("@machine: aux2"), equalTo(expectedMatcher));
	}

	@Test
	public void containsCriteria() throws InvalidQueryException {
		String needle = "needle";
		LogEntryMatcher expectedMatcher = new ContainsMatcher(needle);

		assertThat(translate("contains: " + needle), equalTo(expectedMatcher));
	}

	private LogEntryMatcher translate(String query) throws InvalidQueryException {
		LogEntryMatcher matcher = translator.translate(query).get(0);
		if (matcher == null) {
			throw new AssertionError("No matcher created for: " + query);
		}
		return matcher;
	}
}
