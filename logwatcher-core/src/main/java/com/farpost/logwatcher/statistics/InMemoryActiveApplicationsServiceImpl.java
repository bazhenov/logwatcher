package com.farpost.logwatcher.statistics;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class InMemoryActiveApplicationsServiceImpl implements ActiveApplicationsService {

	private Set<String> activeApplications = newHashSet();

	@Override
	public Set<String> getActiveApplications() {
		return activeApplications;
	}

	public void register(String applicationId) {
		activeApplications.add(applicationId);
	}
}
