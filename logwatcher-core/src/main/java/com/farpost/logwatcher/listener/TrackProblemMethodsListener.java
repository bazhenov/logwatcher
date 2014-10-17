package com.farpost.logwatcher.listener;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.transport.LogEntryListener;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashSet;
import java.util.Set;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.google.common.base.Preconditions.checkNotNull;

public class TrackProblemMethodsListener implements LogEntryListener {

	private final SimpleChecksumCalculator calculator = new SimpleChecksumCalculator();
	private final Set<ClusterReference> trackedClusterReferences = new HashSet<ClusterReference>();
	private final JavaStackTraceParser parser;

	public TrackProblemMethodsListener(JavaStackTraceParser parser) {
		this.parser = checkNotNull(parser);
	}

	@Override
	public void onEntry(LogEntry entry) {
		if (!"search".equalsIgnoreCase(entry.getApplicationId()))
			return;
		if (entry.getCause() == null)
			return;

		Checksum cs = fromHexString(calculator.calculateChecksum(entry));
		Cause cause = entry.getCause();
		while (cause != null) {
			for (StackTraceLine line : parser.parse(cause.getStackTrace()))
				synchronized (trackedClusterReferences) {
					trackedClusterReferences.add(new ClusterReference(line, cs));
				}
			cause = cause.getCause();
		}
	}

	public Set<ClusterReference> getTrackedClusterReferences() {
		synchronized (trackedClusterReferences) {
			return new HashSet<ClusterReference>(trackedClusterReferences);
		}
	}

	public static final class ClusterReference {

		private final StackTraceLine stackTraceLine;
		private final Checksum clusterChecksum;

		public ClusterReference(StackTraceLine stackTraceLine, Checksum clusterChecksum) {
			this.stackTraceLine = checkNotNull(stackTraceLine);
			this.clusterChecksum = checkNotNull(clusterChecksum);
		}

		@JsonIgnore
		public StackTraceLine getStackTraceLine() {
			return stackTraceLine;
		}

		@JsonIgnore
		public Checksum getClusterChecksum() {
			return clusterChecksum;
		}

		@JsonProperty("cluster")
		public String getClusterChecksumAsString() {
			return clusterChecksum.toString();
		}

		@JsonProperty("class")
		public String getClassName() {
			return stackTraceLine.getClassName();
		}

		@JsonProperty("method")
		public String getMethodName() {
			return stackTraceLine.getMethodName();
		}

		@JsonProperty("file")
		public String getFileName() {
			return stackTraceLine.getFileName();
		}

		@JsonProperty("line")
		public int getLineNo() {
			return stackTraceLine.getLineNo();
		}
	}
}
