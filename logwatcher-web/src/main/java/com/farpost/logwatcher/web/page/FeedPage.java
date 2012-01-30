package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.ByLastOccurrenceDateComparator;
import com.farpost.logwatcher.ByOccurenceCountComparator;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.web.JiraInfo;
import com.farpost.logwatcher.web.ViewNameAwarePage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Collections.sort;
import static org.joda.time.LocalDate.fromDateFields;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FeedPage implements ViewNameAwarePage, InitializingBean {

	private LogStorage storage;

	private JiraInfo jiraInfo;

	private HttpServletRequest request;
	private Date date;
	private String applicationId;
	private Severity severity;
	private JSONArray pieChartData;

	private static final Logger log = getLogger(FeedPage.class);

	private final HashMap<String, Comparator<AggregatedEntry>> comparators = new HashMap<String, Comparator<AggregatedEntry>>() {{
		put(null, new ByLastOccurrenceDateComparator());
		put("last-occurence", new ByLastOccurrenceDateComparator());
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
		entries = storage.getAggregatedEntries(applicationId, fromDateFields(date), severity);
		entriesCount = sumCount(entries);
		Comparator<AggregatedEntry> comparator = comparators.containsKey(this.sortOrder)
			? comparators.get(this.sortOrder)
			: comparators.get(null);
		sort(entries, comparator);

		pieChartData = new JSONArray();
		for (AggregatedEntry entry : entries) {
			try {
				String label = entry.getMessage();

				pieChartData.put(new JSONObject()
					.put("label", label)
					.put("data", entry.getCount()));
			} catch (JSONException e) {
				log.warn("Unable to serialize data to JSON", e);
			}
		}
	}

	@Autowired
	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@Autowired
	public void setJiraInfo(JiraInfo jiraInfo) {
		this.jiraInfo = jiraInfo;
	}

	public JiraInfo getJiraInfo() {
		return jiraInfo;
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

	public JSONArray getDataForPieChart() {
		return pieChartData;
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
