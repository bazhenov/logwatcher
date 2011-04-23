package com.farpost.logwatcher.web.page;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.storage.LogStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Date;

import static com.farpost.logwatcher.storage.LogEntries.entries;

public class DetailsPage {

	private String applicationId;
	private Collection<LogEntry> entries;
	private String checksum;
	private Date date;

	@Autowired
	private LogStorage storage;

	public DetailsPage init(String applicationId, String checksum, Date date) {
		this.applicationId = applicationId;
		this.checksum = checksum;
		this.date = date;

		entries = entries().
			applicationId(applicationId).
			checksum(checksum).
			date(new com.farpost.timepoint.Date(date)).
			find(storage);
		
		return this;
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
