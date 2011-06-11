package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.web.page.DetailsPage;
import com.farpost.logwatcher.web.page.FeedPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
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

	@RequestMapping("/feed/{applicationId}")
	@ModelAttribute("p")
	public FeedPage handleFeed(@PathVariable String applicationId,
														 @RequestParam(required = false) @DateTimeFormat(iso = DATE) java.util.Date date,
														 HttpServletRequest request) {
		if (date == null) {
			date = new java.util.Date();
		}

		Severity severity = getSeverity(request);
		String sortOrder = getSortOrder(request);

		return new FeedPage(request, date, applicationId, severity, sortOrder);
	}

	@RequestMapping("/entries/{applicationId}/{checksum}")
	@ModelAttribute("p")
	public DetailsPage handleEntries(@PathVariable String checksum, @PathVariable String applicationId,
																	 @RequestParam @DateTimeFormat(iso = DATE) java.util.Date date) {
		return new DetailsPage(applicationId, checksum, date);
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
