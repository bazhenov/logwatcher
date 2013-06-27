package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.DayStatistic;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.web.page.DetailsPage;
import com.farpost.logwatcher.web.page.FeedPage;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.joda.time.LocalDate.fromDateFields;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class FeedController {

	@Autowired
	private LogStorage storage;

	@Autowired
	private ClusterStatistic clusterStatistic;

	@Autowired
	private ClusterDao clusterDao;

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

	@RequestMapping("/service/feed/{applicationId}")
	public ModelAndView handleInnerFeed(@PathVariable String applicationId,
																			@RequestParam(required = false) @DateTimeFormat(iso = DATE) Date date,
																			HttpServletRequest request) {
		if (date == null) {
			date = new java.util.Date();
		}

		Severity severity = getSeverity(request);
		String sortOrder = getSortOrder(request);

		return new ModelAndView("feed/inner-feed", "p", new InnerFeedPage(date, applicationId, severity, sortOrder));
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
		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, LocalDate.now(), severity);

		//Comparator<AggregatedEntry> comparator = comparators.get("last-occurence");
		//sort(entries, comparator);

		map.addAttribute("entries", entries);
		map.addAttribute("date", new java.util.Date());

		return "feed-rss";
	}

	@RequestMapping("/rest/feed/{applicationId}")
	@ResponseBody
	public Collection<AggregatedEntry> handleFeed(@PathVariable String applicationId) {
		return storage.getAggregatedEntries(applicationId, new LocalDate(), Severity.debug);
	}

	@RequestMapping("/app/{applicationId}")
	public ModelAndView handleApplicationView(@PathVariable String applicationId) {
		return new ModelAndView("application", "p", new ApplicationViewPage(applicationId));
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

	public class ApplicationViewPage {

		private final String applicationId;

		public ApplicationViewPage(String applicationId) {
			this.applicationId = applicationId;
		}

		public String getApplicationId() {
			return applicationId;
		}
	}

	private static int sumCount(List<AggregatedEntry> entries) {
		int times = 0;
		for (AggregatedEntry e : entries) {
			times += e.getCount();
		}
		return times;
	}

	public class InnerFeedPage {

		private final LocalDate date;
		private String applicationId;

		private final HashMap<String, Comparator<AggregatedEntry>> comparators = new HashMap<String, Comparator<AggregatedEntry>>() {{
			put(null, new ByTitleComparator());
			put("last-occurence", new ByLastOccurrenceDateComparator());
			put("occurence-count", new ByOccurenceCountComparator());
		}};
		private List<AggregatedEntry> entries;
		private String sortOrder;
		private int entriesCount;
		private Collection<Cluster> clusters;

		public InnerFeedPage(Date date, String applicationId, Severity severity, String sortOrder) {
			this.applicationId = applicationId;
			this.sortOrder = sortOrder;

			entries = storage.getAggregatedEntries(applicationId, fromDateFields(date), severity);
			entriesCount = sumCount(entries);
			Comparator<AggregatedEntry> comparator = comparators.containsKey(this.sortOrder)
				? comparators.get(this.sortOrder)
				: comparators.get(null);
			sort(entries, comparator);

			this.date = new LocalDate(date.getTime());
			Collection<Checksum> checksums = clusterStatistic.getActiveClusterChecksums(applicationId, this.date);

			clusters = newArrayList();
			for (Checksum checksum : checksums) {
				clusters.add(clusterDao.findCluster(applicationId, checksum));
			}
		}

		public int getEntriesCount() {
			return entriesCount;
		}

		public Collection<AggregatedEntry> getEntries() {
			return entries;
		}

		public Collection<Cluster> getClusters() {
			return clusters;
		}

		public DayStatistic getStatistics(Cluster c) {
			return clusterStatistic.getDayStatistic(applicationId, c.getChecksum(), new LocalDate(date));
		}

		public String getApplicationId() {
			return applicationId;
		}
	}
}
