package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.ByOccurenceDateComparator;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.web.ViewNameAwarePage;
import com.google.common.collect.Ordering;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;

import static com.farpost.logwatcher.storage.LogEntries.entries;

@Component
public class DetailsPage implements ViewNameAwarePage, InitializingBean {

	@Autowired
	private LogStorage storage;

	private String applicationId;
	private Collection<LogEntry> entries;
	private String checksum;
	private Date date;

	public DetailsPage(String applicationId, String checksum, Date date) {
		this.applicationId = applicationId;
		this.checksum = checksum;
		this.date = date;
	}

	@Override
	public String getViewName() {
		return "entries";
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		entries = entries().
			applicationId(applicationId).
			checksum(checksum).
			date(new DateTime(date)).
			find(storage);
		entries = Ordering.from(new ByOccurenceDateComparator()).sortedCopy(entries);
	}

	public Date getDate() {
		return date;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Collection<LogEntry> getEntries() {
		return entries;
	}
}
