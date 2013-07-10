package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.ByLastOccurrenceDateComparator;
import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ByDayStatistic;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.MinuteVector;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.google.common.collect.Ordering;
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
import java.util.*;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
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

	private final static int feedSize = 100;

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
	public ModelAndView handleFeed(@PathVariable String applicationId,
																 @RequestParam(required = false) @DateTimeFormat(iso = DATE) java.util.Date date,
																 HttpServletRequest request) {
		if (date == null) {
			date = new java.util.Date();
		}

		Severity severity = getSeverity(request);
		return new ModelAndView("aggregated-feed", "p", new FeedPage(request, date, applicationId, severity));
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
	public ModelAndView handleDetails(@PathVariable String checksum, @PathVariable String applicationId) {
		Checksum cs = fromHexString(checksum);

		return new ModelAndView("entries", "p", new DetailsPage(applicationId, cs));
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

	public class InnerFeedPage {

		private final LocalDate date;
		private final String applicationId;

		private int entriesCount;
		private final Collection<Cluster> clusters;

		private final Map<Checksum, ByDayStatistic> dayStatisticMap = newHashMap();

		public InnerFeedPage(Date date, String applicationId, Severity severity) {
			this.applicationId = applicationId;

			this.date = LocalDate.fromDateFields(date);
			Collection<Checksum> checksums = clusterStatistic.getActiveClusterChecksums(applicationId, this.date);

			Collection<Cluster> clusters = newArrayList();
			for (Checksum checksum : checksums) {
				Cluster cluster = clusterDao.findCluster(applicationId, checksum);
				if (cluster.getSeverity().isEqualOrMoreImportantThan(severity)) {
					ByDayStatistic dayStatistic = clusterStatistic.getByDayStatistic(applicationId, checksum);
					dayStatisticMap.put(checksum, dayStatistic);
					entriesCount += dayStatistic.getCount(this.date);
					clusters.add(cluster);
				}
			}
			this.clusters = Ordering
				.from(new ByLastOccurrenceDateComparator(dayStatisticMap))
				.sortedCopy(clusters);
		}

		public int getEntriesCount() {
			return entriesCount;
		}

		public LocalDate getDate() {
			return date;
		}

		public Collection<Cluster> getClusters() {
			return clusters;
		}

		public ByDayStatistic getStatistics(Cluster c) {
			return dayStatisticMap.get(c.getChecksum());
		}

		public MinuteVector getMinuteVector(Cluster c) {
			return clusterStatistic.getMinuteVector(applicationId, c.getChecksum());
		}

	}

	public class DetailsPage {

		private final Cluster cluster;
		private final ByDayStatistic statistics;

		public DetailsPage(String applicationId, Checksum checksum) {
			cluster = clusterDao.findCluster(applicationId, checksum);
			statistics = clusterStatistic.getByDayStatistic(applicationId, checksum);
		}

		public Cluster getCluster() {
			return cluster;
		}

		public ByDayStatistic getStatistics() {
			return statistics;
		}
	}

	public class FeedPage {

		private HttpServletRequest request;
		private Date date;
		private String applicationId;
		private Severity severity;

		public FeedPage(HttpServletRequest request, Date date, String applicationId, Severity severity) {
			this.request = request;
			this.date = date;
			this.applicationId = applicationId;
			this.severity = severity;
		}


		public Severity getSeverity() {
			return severity;
		}

		public Date getDate() {
			return date;
		}

		/**
		 * Возвращает множество всех уникальных идентификаторов приложений исключая текущее приложение
		 *
		 * @return множество всех уникальных идентификаторов приложений
		 */
		public Set<Application> getApplications() {
			Set<String> applicationIds = storage.getUniqueApplicationIds(fromDateFields(date));
			Set<Application> set = new HashSet<Application>();
			for (String applicationId : applicationIds) {
				String dateAsString = fromDateFields(date).toString();
				String url = request.getContextPath() + "/feed/" + applicationId + "?date=" + dateAsString;
				set.add(new Application(applicationId, url));
			}
			return set;
		}

		public String getApplicationId() {
			return applicationId;
		}
	}

	public static class Application {

		private final String id;
		private final String url;

		public Application(String id, String url) {
			this.id = id;
			this.url = url;
		}

		public String getId() {
			return id;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Application that = (Application) o;

			return !(id != null ? !id.equals(that.id) : that.id != null);
		}

		@Override
		public int hashCode() {
			return id != null ? id.hashCode() : 0;
		}
	}
}
