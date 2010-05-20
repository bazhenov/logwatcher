package org.bazhenov.logging.web;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.bazhenov.logging.storage.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.farpost.timepoint.Date.today;
import static java.util.Collections.sort;
import static org.bazhenov.logging.storage.LogEntries.entries;

@Controller
public class FeedController {

	private final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	private LogStorage storage;
	private final QueryTranslator translator = new AnnotationDrivenQueryTranslator(
		new TranslationRulesImpl());
	private final QueryParser parser = new QueryParser();
	private final Comparator<AggregatedEntry> byOccurenceCount = new ByOccurenceCountComparator();
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
	public View removeEntry(@RequestParam("checksum") String checksum) throws LogStorageException {
		storage.removeEntries(checksum);
		return new BufferView("Ok");
	}

	@RequestMapping("/search")
	public String handleSearch(@RequestParam(value = "q", required = false) String query, ModelMap map)
		throws InvalidQueryException, LogStorageException, InvalidCriteriaException {
		if (query == null || query.isEmpty()) {
			return "search-form";
		}
		List<AggregatedEntry> entries;
		List<LogEntryMatcher> matchers;
		try {
			matchers = translator.translate(query.trim());
		} catch (InvalidQueryException e) {
			return "invalid-query";
		}
		if (!contains(matchers, DateMatcher.class)) {
			matchers.add(new DateMatcher(today()));
		}
		if (!contains(matchers, SeverityMatcher.class)) {
			matchers.add(new SeverityMatcher(Severity.error));
		}
		entries = entries().withCriteria(matchers).findAggregated(storage);
		map.put("entries", entries);
		int times = sumCount(entries);
		map.put("times", times);
		map.put("date", DateTime.now().asDate());
		Map<String, String> queryTerms = parser.parse(query);
		map.put("query", buildQuery(queryTerms));
		map.put("terms", queryTerms);
		return "search-feed";
	}

	@RequestMapping("/dashboard")
	public String doDashboard(ModelMap map) throws LogStorageException {
		List<AggregatedEntry> allEntries = storage.getAggregatedEntries(today(), Severity.trace);
		List<ApplicationInfo> infos = groupEntriesByApplicationId(allEntries);
		map.put("infos", infos);
		return "dashboard";
	}

	private List<ApplicationInfo> groupEntriesByApplicationId(List<AggregatedEntry> allEntries) {
		List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
		for (String applicationId : getUniqueApplicationId(allEntries)) {
			List<AggregatedEntry> applicationsEntries = filter(allEntries, applicationId);
			sort(applicationsEntries, byOccurenceCount);
			ApplicationInfo info = new ApplicationInfo(applicationId, applicationsEntries);
			infos.add(info);
		}
		return infos;
	}

	@RequestMapping("/feed/{applicationId}")
	public String handleFeed(@PathVariable String applicationId,
													 ModelMap map, HttpServletRequest request, HttpServletResponse response)
		throws ParseException, LogStorageException, InvalidCriteriaException, InvalidQueryException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		Date today = today();
		Date date;
		String dateStr = request.getParameter("date");
		if (dateStr == null) {
			date = today;
		} else {
			date = new Date(format.get().parse(dateStr).getTime());
		}

		map.addAttribute("date", date.asDate());

		Severity severity = getSeverity(request);
		map.addAttribute("severity", severity.toString());

		List<AggregatedEntry> entries = storage.getAggregatedEntries(date, severity);

		int times = sumCount(entries);
		String sortOrder = getSortOrder(request);
		Comparator<AggregatedEntry> comparator = comparators.containsKey(sortOrder)
			? comparators.get(sortOrder)
			: comparators.get(null);
		sort(entries, comparator);
		map.addAttribute("sortOrder", sortOrder);

		map.addAttribute("applicationId", applicationId);

		Set<String> uniqueApplicationIds = getUniqueApplicationId(storage.getAggregatedEntries(date, Severity.trace));
		map.addAttribute("uniqueApplicationIds", uniqueApplicationIds);
		map.addAttribute("entries", filter(entries, applicationId));
		map.addAttribute("times", times);

		return "aggregated-feed";
	}

	private static int sumCount(List<AggregatedEntry> entries) {
		int times = 0;
		for (AggregatedEntry e : entries) {
			times += e.getCount();
		}
		return times;
	}

	private List<AggregatedEntry> filter(List<AggregatedEntry> entries, String applicationId) {
		List<AggregatedEntry> result = new ArrayList<AggregatedEntry>();
		for (AggregatedEntry entry : entries) {
			if (entry.getApplicationId().equals(applicationId)) {
				result.add(entry);
			}
		}
		return result;
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
	public String handleEntries(@PathVariable String checksum, ModelMap map, @RequestParam("date") String dateAsString)
		throws LogStorageException, InvalidCriteriaException, ParseException {

		java.util.Date date = format.get().parse(dateAsString);
		List<LogEntry> entries = entries().
			checksum(checksum).
			date(new Date(date)).
			find(storage);
		map.addAttribute("checksum", checksum);
		map.addAttribute("entries", entries);
		map.addAttribute("date", date);
		map.addAttribute("applicationId", entries.get(0).getApplicationId());
		return "entries";
	}

	private boolean contains(List<LogEntryMatcher> matchers, Class<? extends LogEntryMatcher> type) {
		for (LogEntryMatcher matcher : matchers) {
			if (matcher.getClass().equals(type)) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping("/feed/rss")
	public String handleRss(ModelMap map, @RequestParam(value = "severity", required = false) String s)
		throws LogStorageException, InvalidCriteriaException {

		Severity severity = s == null
			? Severity.error
			: Severity.forName(s);
		List<AggregatedEntry> entries = storage.getAggregatedEntries(today(), severity);

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

	public static Set<String> getUniqueApplicationId(List<AggregatedEntry> entries) {
		Set<String> set = new TreeSet<String>();
		for (AggregatedEntry entry : entries) {
			if (!set.contains(entry.getApplicationId())) {
				set.add(entry.getApplicationId());
			}
		}
		return set;
	}
}
