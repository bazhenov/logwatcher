package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
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

import static com.farpost.timepoint.Date.today;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class FeedController {

	@Autowired
	private LogStorage storage;

	@Autowired
	private BeanFactory beans;

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
	public ModelAndView handleSearch(@RequestParam(required = false) String q) {
		SearchPage p = beans.getBean(SearchPage.class).init(q);
		return new ModelAndView(p.getViewName(), "p", p);
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

		FeedPage p = beans.getBean(FeedPage.class).init(request, date, applicationId, severity, sortOrder);

		return new ModelAndView("feed/aggregated-feed", "p", p);
	}

	@RequestMapping("/entries/{applicationId}/{checksum}")
	public ModelAndView handleEntries(@PathVariable String checksum, @PathVariable String applicationId,
																		@RequestParam @DateTimeFormat(iso = DATE) java.util.Date date) {
		DetailsPage p = beans.getBean(DetailsPage.class).init(applicationId, checksum, date);
		return new ModelAndView("entries", "p", p);
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

	private static Severity getSeverity(HttpServletRequest request) {
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

	private static String getSortOrder(HttpServletRequest request) {
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
