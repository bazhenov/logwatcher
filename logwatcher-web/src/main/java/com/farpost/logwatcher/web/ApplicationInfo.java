package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregatedEntry;

import java.util.List;

public class ApplicationInfo {
	private final String applicationId;
	private final List<AggregatedEntry> entries;

	public ApplicationInfo(String applicationId, List<AggregatedEntry> entries) {
		this.applicationId = applicationId;
		this.entries = entries;
	}

	public List<AggregatedEntry> getEntries() {
		return entries;
	}

	public String getApplicationId() {
		return applicationId;
	}
}
