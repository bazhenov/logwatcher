package com.farpost.logwatcher.web.vm;

import com.farpost.logwatcher.storage.LogStorage;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FeedViewModel {

	private final HttpServletRequest request;
	private final LogStorage storage;
	private final Date date;
	private final String applicationId;

	public FeedViewModel(HttpServletRequest request, LogStorage storage, Date date, String applicationId) {
		this.request = request;
		this.storage = storage;
		this.date = new Date(date.getTime());
		this.applicationId = applicationId;
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
