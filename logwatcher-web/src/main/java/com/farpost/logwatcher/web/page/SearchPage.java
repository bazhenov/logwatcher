package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.storage.DateMatcher;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.SeverityMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.farpost.timepoint.Date.today;

@Component
public class SearchPage {

	@Autowired
	LogStorage storage;

	@Autowired
	private QueryTranslator translator;

	private String query;
	private List<LogEntry> entries;
	private String viewName;

	public SearchPage init(String query) {
		this.query = query;

		if (query == null || query.isEmpty()) {
			viewName = "search-form";
			return this;
		}
		try {
			List<LogEntryMatcher> matchers = translator.translate(query.trim());
			if (!contains(matchers, DateMatcher.class)) {
				matchers.add(new DateMatcher(today()));
			}
			if (!contains(matchers, SeverityMatcher.class)) {
				matchers.add(new SeverityMatcher(Severity.error));
			}
			entries = entries().withCriteria(matchers).find(storage);
			viewName = "search-page";
		} catch (InvalidQueryException e) {
			viewName = "invalid-query";
		}
		return this;
	}

	private boolean contains(List<LogEntryMatcher> matchers, Class<? extends LogEntryMatcher> type) {
		for (LogEntryMatcher matcher : matchers) {
			if (matcher.getClass().equals(type)) {
				return true;
			}
		}
		return false;
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

	public String getViewName() {
		return viewName;
	}
}
