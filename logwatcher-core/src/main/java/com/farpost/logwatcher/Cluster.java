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

	@Nullable
	private String customTitle;

	private String originalTitle;
	private String description;
	private final String applicationId;
	private final Checksum checksum;

	@Nullable
	private String issueKey;

	public Cluster(String applicationId, String originalTitle, Checksum checksum, String customTitle, String description,
								 String issueKey) {
		this.applicationId = applicationId;
		this.originalTitle = checkNotNull(originalTitle);
		this.checksum = checkNotNull(checksum);
		this.customTitle = customTitle;
		this.description = description;
		this.issueKey = issueKey;
	}

	public Cluster(String applicationId, String originalTitle, byte[] checksum) {
		this(applicationId, originalTitle, new Checksum(checksum), null, null, null);
	}

	public Cluster(String applicationId, String originalTitle, Checksum checksum) {
		this(applicationId, originalTitle, checksum, null, null, null);
	}

	/**
	 * @return custom title for a cluster. This is title given for a cluster by a end user
	 */
	@Nullable
	public String getCustomTitle() {
		return customTitle;
	}

	/**
	 * @return original title for a cluster, that is log entry title from which this cluster was created
	 */
	public String getOriginalTitle() {
		return originalTitle;
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

	public Checksum getChecksum() {
		return checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Cluster)) return false;

		Cluster that = (Cluster) o;
		return equal(checksum, that.checksum) && equal(description, that.description) && equal(issueKey, that.issueKey) &&
			equal(customTitle, that.customTitle) && equal(originalTitle, that.originalTitle) &&
			equal(applicationId, that.applicationId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(customTitle, issueKey, description, checksum, originalTitle, applicationId);
	}
}
