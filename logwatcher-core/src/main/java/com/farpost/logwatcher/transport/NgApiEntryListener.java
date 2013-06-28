package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.InMemoryActiveApplicationsServiceImpl;
import com.farpost.logwatcher.storage.LogStorage;

import static com.farpost.logwatcher.Checksum.fromHexString;

public class NgApiEntryListener implements LogEntryListener {

	private final LogStorage storage;
	private final ClusterDao clusterDao;
	private final ClusterStatistic clusterStatistic;
	private final InMemoryActiveApplicationsServiceImpl activeApplications;

	private final ChecksumCalculator checksumCalculator = new SimpleChecksumCalculator();

	public NgApiEntryListener(LogStorage storage, ClusterDao clusterDao, ClusterStatistic clusterStatistic,
														InMemoryActiveApplicationsServiceImpl activeApplications) {
		this.storage = storage;
		this.clusterDao = clusterDao;
		this.clusterStatistic = clusterStatistic;
		this.activeApplications = activeApplications;
	}

	@Override
	public void onEntry(LogEntry entry) {
		Checksum checksum = fromHexString(checksumCalculator.calculateChecksum(entry));
		if (!clusterDao.isClusterRegistered(entry.getApplicationId(), checksum)) {
			clusterDao.registerCluster(new Cluster(entry.getApplicationId(), entry.getSeverity(), entry.getMessage(),
				checksum));
		}
		clusterStatistic.registerEvent(entry.getApplicationId(), entry.getDate(), checksum);
		activeApplications.register(entry.getApplicationId());
		storage.writeEntry(entry);
	}
}
