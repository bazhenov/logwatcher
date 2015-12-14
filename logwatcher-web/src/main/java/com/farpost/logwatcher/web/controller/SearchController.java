package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.storage.DateMatcher;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.SeverityMatcher;
import com.farpost.logwatcher.web.AttributeFormatter;
import com.farpost.logwatcher.web.LogEntryClassifier;
import com.google.common.collect.Ordering;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.farpost.logwatcher.storage.LogEntries.entries;

@Controller
public class SearchController {

	@Autowired
	private LogStorage storage;

	@Autowired
	private QueryTranslator translator;

	@Autowired
	private AttributeFormatter formatter;

	@Autowired
	private LogEntryClassifier entryClassifier;

	@RequestMapping("/search")
	@ModelAttribute("p")
	public ModelAndView handleSearch(@RequestParam(required = false) String q) {
		if (q == null || q.isEmpty()) {
			return new ModelAndView("search-form");
		} else {
			try {
				List<LogEntryMatcher> matchers = translator.translate(q.trim());
				if (!contains(matchers, DateMatcher.class)) {
					matchers.add(new DateMatcher(LocalDate.now()));
				}
				if (!contains(matchers, SeverityMatcher.class)) {
					matchers.add(new SeverityMatcher(Severity.error));
				}

				return new ModelAndView("search-page", "p", new SearchPage(q, matchers));
			} catch (InvalidQueryException e) {
				return new ModelAndView("invalid-query");
			}
		}
	}

	private static boolean contains(List<LogEntryMatcher> matchers, Class<? extends LogEntryMatcher> type) {
		for (LogEntryMatcher matcher : matchers) {
			if (matcher.getClass().equals(type)) {
				return true;
			}
		}
		return false;
	}

	public class SearchPage {

		private String query;
		private List<LogEntry> entries;

		public SearchPage(String query, List<LogEntryMatcher> matchers) {
			this.query = query;
			entries = entries().withCriteria(matchers).find(storage);
			entries = Ordering.from(new ByOccurrenceDateComparator()).greatestOf(entries, 100);
		}


		public List<LogEntry> getEntries() {
			return entries;
		}

		public AttributeFormatter getFormatter() {
			return formatter;
		}

		public LogEntryClassifier getClassifier() {
			return entryClassifier;
		}

		public String getQuery() {
			return query;
		}
	}
}
