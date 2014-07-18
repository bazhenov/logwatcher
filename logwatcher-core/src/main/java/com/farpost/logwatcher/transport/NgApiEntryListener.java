package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.storage.LogStorage;
import org.joda.time.DateTime;

import static com.farpost.logwatcher.Checksum.fromHexString;

public class NgApiEntryListener implements LogEntryListener {

	private final LogStorage storage;
	private final ClusterDao clusterDao;
	private final ClusterStatistic clusterStatistic;

	private final ChecksumCalculator checksumCalculator = new SimpleChecksumCalculator();

	public NgApiEntryListener(LogStorage storage, ClusterDao clusterDao, ClusterStatistic clusterStatistic) {
		this.storage = storage;
		this.clusterDao = clusterDao;
		this.clusterStatistic = clusterStatistic;
	}

	@Override
	public synchronized void onEntry(LogEntry entry) {
		Checksum checksum = fromHexString(checksumCalculator.calculateChecksum(entry));
		Cluster cluster = new Cluster(entry.getApplicationId(), entry.getSeverity(), entry.getMessage(), checksum);
		if (entry.getCause() != null)
			cluster.setCauseType(entry.getCause().getType());
		cluster.setGroup(entry.getGroup());
		clusterDao.registerCluster(cluster);
		clusterStatistic.registerEvent(entry.getApplicationId(), new DateTime(entry.getDate()), checksum,
			entry.getSeverity());
		storage.writeEntry(entry);
	}
}
