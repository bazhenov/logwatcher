package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.google.common.base.Function;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

public class InMemoryClusterDao implements ClusterDao {

	private final Map<String, Map<Checksum, Cluster>> clusterMap = newHashMap();

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
		map.put(cluster.getChecksum(), cluster);
	}

	@Override
	public Cluster findCluster(String applicationId, Checksum checksum) {
		return clusterMap.containsKey(applicationId)
			? clusterMap.get(applicationId).get(checksum)
			: null;
	}

	@Override
	public void changeCluster(String applicationId, Checksum checksum, Function<Cluster, Void> f) {
		Cluster cluster = findCluster(applicationId, checksum);
		checkArgument(cluster != null, "Cluster not found");
		f.apply(cluster);
	}
}

