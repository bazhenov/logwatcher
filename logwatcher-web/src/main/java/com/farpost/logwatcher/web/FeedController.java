package com.farpost.logwatcher.web;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.storage.*;
import com.farpost.logwatcher.web.page.DetailsPage;
import com.farpost.logwatcher.web.page.FeedPage;
import com.farpost.logwatcher.web.page.SearchPage;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.farpost.timepoint.Date.today;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class FeedController {

	private LogStorage storage;
	private final QueryTranslator translator = new AnnotationDrivenQueryTranslator(new TranslationRulesImpl());

	@Autowired
	private BeanFactory beans;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/")
	public View handleRoot() {
		return new RedirectView("/dashboard", true);
	}

	@RequestMapping("/entry/remove")
	@ResponseBody
	public String removeEntry(@RequestParam("checksum") String checksum) throws LogStorageException {
		storage.removeEntriesWithChecksum(checksum);
		return "Ok";
	}

	@RequestMapping("/search")
	public String handleSearch(@RequestParam(required = false) String q, ModelMap map)
		throws InvalidQueryException, LogStorageException, InvalidCriteriaException {

		if (q == null || q.isEmpty()) {
			return "search-form";
		}
		List<LogEntryMatcher> matchers;
		try {
			matchers = translator.translate(q.trim());
		} catch (InvalidQueryException e) {
			return "invalid-query";
		}
		if (!contains(matchers, DateMatcher.class)) {
			matchers.add(new DateMatcher(today()));
		}
		if (!contains(matchers, SeverityMatcher.class)) {
			matchers.add(new SeverityMatcher(Severity.error));
		}
		List<LogEntry> entries = entries().withCriteria(matchers).find(storage);

		map.put("p", new SearchPage(q, entries));
		return "search-page";
	}

	@RequestMapping("/feed/{applicationId}")
	public ModelAndView handleFeed(@PathVariable String applicationId,
																 @RequestParam(required = false) @DateTimeFormat(iso = DATE) java.util.Date date,
																 HttpServletRequest request) {
		if (date == null) {
			date = new java.util.Date();
		}

		Severity severity = getSeverity(request);
		String sortOrder = getSortOrder(request);

		FeedPage p = beans.getBean(FeedPage.class).init(request, date, applicationId, severity);
		p.setSortOrder(sortOrder);

		return new ModelAndView("feed/aggregated-feed", "p", p);
	}

	@RequestMapping("/entries/{applicationId}/{checksum}")
	public ModelAndView handleEntries(@PathVariable String checksum, @PathVariable String applicationId,
																		@RequestParam @DateTimeFormat(iso = DATE) java.util.Date date) {
		DetailsPage p = beans.getBean(DetailsPage.class).init(applicationId, checksum, date);
		return new ModelAndView("entries", "p", p);
	}

	private boolean contains(List<LogEntryMatcher> matchers, Class<? extends LogEntryMatcher> type) {
		for (LogEntryMatcher matcher : matchers) {
			if (matcher.getClass().equals(type)) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping("/rss/{applicationId}")
	public String handleRss(ModelMap map, @RequestParam(value = "severity", required = false) String s,
													@PathVariable String applicationId) throws LogStorageException, InvalidCriteriaException {
		Severity severity = (s == null)
			? Severity.error
			: Severity.forName(s);
		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, today(), severity);

		//Comparator<AggregatedEntry> comparator = comparators.get("last-occurence");
		//sort(entries, comparator);

		map.addAttribute("entries", entries);
		map.addAttribute("date", new java.util.Date());

		return "feed-rss";
	}

	private Severity getSeverity(HttpServletRequest request) {
		String get = request.getParameter("severity");
		if (get != null) {
			return Severity.forName(get);
		}

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("severity".equals(cookie.getName())) {
					return Severity.forName(cookie.getValue());
				}
			}
		}
		return Severity.error;
	}

	private String getSortOrder(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("sortOrder".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
