package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Cluster;

public interface ClusterDao {

	boolean isClusterRegistered(byte[] checksum);

	void registerCluster(Cluster cluster);
}
