package org.bazhenov.logging.web;

import org.bazhenov.logging.AggregatedEntry;

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
}
