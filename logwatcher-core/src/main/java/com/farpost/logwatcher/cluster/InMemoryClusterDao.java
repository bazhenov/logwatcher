package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

public class InMemoryClusterDao implements ClusterDao {

	private final Map<Checksum, Cluster> clusterMap = newHashMap();

	@Override
	public boolean isClusterRegistered(Checksum checksum) {
		return clusterMap.containsKey(checksum);
	}

	@Override
	public synchronized void registerCluster(Cluster cluster) {
		checkArgument(!clusterMap.containsKey(cluster.getChecksum()), "Cluster already registered");
		clusterMap.put(cluster.getChecksum(), cluster);
	}
}

