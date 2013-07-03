package com.farpost.logwatcher;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents the group of log messages ({@link LogEntry}) that grouped in cluster based on checksum
 * equality. This group is called cluster, and have it's own preperties like: custom customTitle or issue tracker key.
 */
public final class Cluster {

	private String title;

	private String description;
	private final String applicationId;
	private final Checksum checksum;
	private final Severity severity;

	@Nullable
	private String issueKey;

	public Cluster(String applicationId, String title, Checksum checksum, String description,
								 String issueKey, Severity severity) {
		this.applicationId = applicationId;
		this.title = checkNotNull(title);
		this.checksum = checkNotNull(checksum);
		this.severity = checkNotNull(severity);
		this.description = description;
		this.issueKey = issueKey;
	}

	public Cluster(String applicationId, Severity severity, String title, Checksum checksum) {
		this(applicationId, title, checksum, null, null, severity);
	}

	/**
	 * @return title for a cluster, that is log entry title from which this cluster was created
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return application id from which this cluster is
	 */
	public String getApplicationId() {
		return applicationId;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Nullable
	public String getIssueKey() {
		return issueKey;
	}

	public Severity getSeverity() {
		return severity;
	}

	public Checksum getChecksum() {
		return checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Cluster)) return false;

		Cluster that = (Cluster) o;
		return equal(checksum, that.checksum) && equal(description, that.description) && equal(issueKey, that.issueKey) &&
			equal(title, that.title) && equal(applicationId, that.applicationId) && equal(severity, that.severity);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(issueKey, description, checksum, title, applicationId, severity);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("applicationId", applicationId)
			.add("checksum", checksum)
			.add("title", title)
			.toString();
	}
}
