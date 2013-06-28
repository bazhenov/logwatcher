package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;

public class NgApiTransportListener implements TransportListener {

	private final LogStorage storage;
	private final Marshaller marshaller;
	private final ClusterDao clusterDao;
	private final ClusterStatistic clusterStatistic;

	public NgApiTransportListener(LogStorage storage, Marshaller marshaller, ClusterDao clusterDao,
																ClusterStatistic clusterStatistic) {
		this.storage = storage;
		this.marshaller = marshaller;
		this.clusterDao = clusterDao;
		this.clusterStatistic = clusterStatistic;
	}

	public void onMessage(byte[] message) throws TransportException {
		try {
			LogEntry entry = marshaller.unmarshall(message);
			Checksum checksum = Checksum.fromHexString(entry.getChecksum());
			if (!clusterDao.isClusterRegistered(entry.getApplicationId(), checksum)) {
				clusterDao.registerCluster(new Cluster(entry.getApplicationId(), entry.getSeverity(), entry.getMessage(),
					checksum));
			}
			clusterStatistic.registerEvent(entry.getApplicationId(), entry.getDate(), checksum);
			storage.writeEntry(entry);
		} catch (LogStorageException e) {
			throw new TransportException(e);
		}
	}
}
