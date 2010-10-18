package com.farpost.logwatcher.web.vm;

import org.bazhenov.logging.AggregatedEntry;
import org.bazhenov.logging.Severity;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.web.FeedController;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class FeedViewModel {

	private final HttpServletRequest request;
	private final LogStorage storage;
	private final Date date;
	private final String applicationId;

	public FeedViewModel(HttpServletRequest request, LogStorage storage, Date date, String applicationId) {
		this.request = request;
		this.storage = storage;
		this.date = date;
		this.applicationId = applicationId;
	}

	/**
	 * Возвращает множество всех уникальных идентификаторов приложений исключая текущее приложение
	 *
	 * @return множество всех уникальных идентификаторов приложений
	 */
	public Set<Application> getApplications() {
		List<AggregatedEntry> entries = storage.getAggregatedEntries(new com.farpost.timepoint.Date(date), Severity.trace);
		return groupByApplication(entries, request);
	}

	/**
	 * Группирует записи по applicationId и возвращает множество всех уникальных
	 * идентификаторов приложений, которые встречаются в записях.
	 *
	 * @param entries записи логов
	 * @param request HTTP-запрос
	 * @return множество всех уникальных идентификаторов приложений
	 */
	private Set<Application> groupByApplication(List<AggregatedEntry> entries, HttpServletRequest request) {
		Set<Application> set = new HashSet<Application>();
		for (AggregatedEntry entry : entries) {
			String id = entry.getApplicationId();
			if (!id.equalsIgnoreCase(applicationId)) {
				String dateAsString = new com.farpost.timepoint.Date(date).toString();
				String url = request.getContextPath() + "/feed/" + id + "?date=" + dateAsString;
				set.add(new Application(id, url));
			}
		}
		return set;

	}

	/**
	 * Группирует записи по applicationId и возвращает множество всех уникальных
	 * идентификаторов приложений, которые встречаются в записях.
	 *
	 * @param entries записи логов
	 * @return множество всех уникальных идентификаторов приложений
	 */
	public static Set<String> groupByApplication(List<AggregatedEntry> entries) {
		Set<String> set = new TreeSet<String>();
		for (AggregatedEntry entry : entries) {
			set.add(entry.getApplicationId());
		}
		return set;
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
