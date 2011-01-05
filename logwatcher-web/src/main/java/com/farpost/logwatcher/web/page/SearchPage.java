package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.InvalidQueryException;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.QueryParser;

import java.util.List;
import java.util.Map;

public class SearchPage {

	private final String query;
	private final List<LogEntry> entries;

	public SearchPage(String query, List<LogEntry> entries) throws InvalidQueryException {
		this.query = query;
		this.entries = entries;
	}

	public List<LogEntry> getEntries() {
		return entries;
	}

	public Map<String, String> getQueryTerms() throws InvalidQueryException {
		return new QueryParser().parse(query);
	}

	public String getQuery() {
		return query;
	}
}
