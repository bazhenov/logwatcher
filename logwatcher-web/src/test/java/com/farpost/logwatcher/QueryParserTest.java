package com.farpost.logwatcher;

import com.farpost.logwatcher.InvalidQueryException;
import com.farpost.logwatcher.QueryParser;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class QueryParserTest {

	QueryParser parser = new QueryParser();

	@Test
	public void parserCanParseSimpleQueries() throws InvalidQueryException {
		Map<String, String> result = parser.parse("occured: 2 days ago at:frontend");

		assertThat(result, hasEntry("occured", "2 days ago"));
		assertThat(result, hasEntry("at", "frontend"));
	}
}
