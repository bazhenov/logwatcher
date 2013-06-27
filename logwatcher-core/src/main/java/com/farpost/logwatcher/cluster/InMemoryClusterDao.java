package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

public class InMemoryClusterDao implements ClusterDao {

	private final Map<String, Map<Checksum, Cluster>> clusterMap = newHashMap();

	@Override
	public boolean isClusterRegistered(String applicationId, Checksum checksum) {
		return clusterMap.containsKey(applicationId) && clusterMap.get(applicationId).containsKey(checksum);
	}

	@Override
	public synchronized void registerCluster(Cluster cluster) {
		Map<Checksum, Cluster> map = clusterMap.get(cluster.getApplicationId());
		if (map == null) {
			map = newHashMap();
			clusterMap.put(cluster.getApplicationId(), map);
		}
		checkArgument(!map.containsKey(cluster.getChecksum()), "Cluster already registered");
		map.put(cluster.getChecksum(), cluster);
	}

	@Override
	public Cluster findCluster(String applicationId, Checksum checksum) {
		return clusterMap.containsKey(applicationId)
			? clusterMap.get(applicationId).get(checksum)
			: null;
	}
}

