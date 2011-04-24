package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.ByLastOccurenceDateComparator;
import com.farpost.logwatcher.ByOccurenceCountComparator;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.web.ViewNameAwarePage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Collections.sort;

public class FeedPage implements ViewNameAwarePage, InitializingBean {

	@Autowired
	private LogStorage storage;

	private HttpServletRequest request;
	private Date date;
	private String applicationId;
	private Severity severity;

	private final HashMap<String, Comparator<AggregatedEntry>> comparators = new HashMap<String, Comparator<AggregatedEntry>>() {{
		put(null, new ByLastOccurenceDateComparator());
		put("last-occurence", new ByLastOccurenceDateComparator());
		put("occurence-count", new ByOccurenceCountComparator());
	}};
	private List<AggregatedEntry> entries;
	private String sortOrder;
	private int entriesCount;

	public FeedPage(HttpServletRequest request, Date date, String applicationId, Severity severity, String sortOrder) {
		this.request = request;
		this.date = date;
		this.applicationId = applicationId;
		this.severity = severity;
		this.sortOrder = sortOrder;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		entries = storage.getAggregatedEntries(applicationId, new com.farpost.timepoint.Date(date), severity);
		entriesCount = sumCount(entries);
		Comparator<AggregatedEntry> comparator = comparators.containsKey(this.sortOrder)
			? comparators.get(this.sortOrder)
			: comparators.get(null);
		sort(entries, comparator);
	}

	@Override
	public String getViewName() {
		return "feed/aggregated-feed";
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public int getEntriesCount() {
		return entriesCount;
	}

	private static int sumCount(List<AggregatedEntry> entries) {
		int times = 0;
		for (AggregatedEntry e : entries) {
			times += e.getCount();
		}
		return times;
	}

	public Severity getSeverity() {
		return severity;
	}

	public Date getDate() {
		return date;
	}

	public Collection<AggregatedEntry> getEntries() {
		return entries;
	}

	/**
	 * Возвращает множество всех уникальных идентификаторов приложений исключая текущее приложение
	 *
	 * @return множество всех уникальных идентификаторов приложений
	 */
	public Set<Application> getApplications() {
		Set<String> applicationIds = storage.getUniquieApplicationIds(new com.farpost.timepoint.Date(date));
		Set<Application> set = new HashSet<Application>();
		for (String applicationId : applicationIds) {
			String dateAsString = new com.farpost.timepoint.Date(date).toString();
			String url = request.getContextPath() + "/feed/" + applicationId + "?date=" + dateAsString;
			set.add(new Application(applicationId, url));
		}
		return set;
	}

	public String getApplicationId() {
		return applicationId;
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
