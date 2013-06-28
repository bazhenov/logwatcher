package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.DayStatistic;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.web.page.DetailsPage;
import com.farpost.logwatcher.web.page.FeedPage;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
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

		return new ModelAndView("feed/inner-feed", "p", new InnerFeedPage(date, applicationId, severity));
	}

	@RequestMapping("/entries/{applicationId}/{checksum}")
	@ModelAttribute("p")
	public DetailsPage handleEntries(@PathVariable String checksum, @PathVariable String applicationId,
																	 @RequestParam @DateTimeFormat(iso = DATE) java.util.Date date) {
		return new DetailsPage(applicationId, checksum, date);
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

	public class InnerFeedPage {

		private final LocalDate date;
		private final String applicationId;

		private int entriesCount;
		private Collection<Cluster> clusters;

		public InnerFeedPage(Date date, String applicationId, Severity severity) {
			this.applicationId = applicationId;

			this.date = new LocalDate(date.getTime());
			Collection<Checksum> checksums = clusterStatistic.getActiveClusterChecksums(applicationId, this.date);

			clusters = newArrayList();
			for (Checksum checksum : checksums) {
				Cluster cluster = clusterDao.findCluster(applicationId, checksum);
				if (cluster.getSeverity().isEqualOrMoreImportantThan(severity)) {
					entriesCount += getStatistics(cluster).getCount();
					clusters.add(cluster);
				}
			}
		}

		public int getEntriesCount() {
			return entriesCount;
		}

		public Collection<Cluster> getClusters() {
			return clusters;
		}

		public DayStatistic getStatistics(Cluster c) {
			return clusterStatistic.getDayStatistic(applicationId, c.getChecksum(), date);
		}

		public String getApplicationId() {
			return applicationId;
		}
	}
}
