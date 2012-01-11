package com.farpost.logwatcher.web;

public class JiraInfo {

	private final String location;
	private final int issueType;
	private final int priority;
	private final int projectPid;

	public JiraInfo(String location, int projectPid, int issueType, int priority) {
		this.location = location;
		this.issueType = issueType;
		this.priority = priority;
		this.projectPid = projectPid;
	}

	public String getLocation() {
		return location;
	}

	public int getIssueType() {
		return issueType;
	}

	public int getPriority() {
		return priority;
	}

	public int getProjectPid() {
		return projectPid;
	}
}
