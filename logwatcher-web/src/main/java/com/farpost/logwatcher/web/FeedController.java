package com.farpost.logwatcher.web;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.storage.*;
import com.farpost.logwatcher.web.page.FeedPage;
import com.farpost.logwatcher.web.page.SearchPage;
import com.farpost.timepoint.Date;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.farpost.timepoint.Date.today;
import static java.util.Collections.sort;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class FeedController {

	private LogStorage storage;
	private final QueryTranslator translator = new AnnotationDrivenQueryTranslator(new TranslationRulesImpl());
	private final QueryParser parser = new QueryParser();
	private final HashMap<String, Comparator<AggregatedEntry>> comparators = new HashMap<String, Comparator<AggregatedEntry>>() {{
		put(null, new ByLastOccurenceDateComparator());
		put("last-occurence", new ByLastOccurenceDateComparator());
		put("occurence-count", new ByOccurenceCountComparator());
	}};

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
	public String handleFeed(@PathVariable String applicationId,
													 @RequestParam(required = false) @DateTimeFormat(iso = DATE) java.util.Date date, ModelMap map,
													 HttpServletRequest request)
		throws ParseException, LogStorageException, InvalidCriteriaException, InvalidQueryException {

		if (date == null) {
			date = new java.util.Date();
		}
		map.addAttribute("date", date);

		Severity severity = getSeverity(request);
		map.addAttribute("severity", severity.toString());

		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, new Date(date), severity);

		int times = sumCount(entries);
		String sortOrder = getSortOrder(request);
		Comparator<AggregatedEntry> comparator = comparators.containsKey(sortOrder)
			? comparators.get(sortOrder)
			: comparators.get(null);
		sort(entries, comparator);
		map.addAttribute("sortOrder", sortOrder);

		map.addAttribute("entries", entries);
		map.addAttribute("times", times);
		map.addAttribute("vm", new FeedPage(request, storage, date, applicationId));

		return "feed/aggregated-feed";
	}

	private static int sumCount(List<AggregatedEntry> entries) {
		int times = 0;
		for (AggregatedEntry e : entries) {
			times += e.getCount();
		}
		return times;
	}

	private String buildQuery(Map<String, String> queryTerms) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> row : queryTerms.entrySet()) {
			builder.
				append(row.getKey()).
				append(": ").
				append(row.getValue()).
				append(" ");
		}
		return builder.toString().trim();
	}

	@RequestMapping("/entries/{checksum}")
	public String handleEntries(@PathVariable String checksum, ModelMap map,
															@RequestParam @DateTimeFormat(iso = DATE) java.util.Date date)
		throws LogStorageException, InvalidCriteriaException, ParseException {

		map.addAttribute("checksum", checksum);
		map.addAttribute("date", date);

		List<LogEntry> entries = entries().
			checksum(checksum).
			date(new Date(date)).
			find(storage);

		if (entries.isEmpty()) {
			return "entries-empty";
		} else {
			map.addAttribute("entries", entries);
			map.addAttribute("applicationId", entries.get(0).getApplicationId());
			return "entries";
		}
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

		Comparator<AggregatedEntry> comparator = comparators.get("last-occurence");
		sort(entries, comparator);

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
