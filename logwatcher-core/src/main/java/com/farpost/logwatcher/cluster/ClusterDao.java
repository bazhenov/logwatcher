package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;

public interface ClusterDao {

	boolean isClusterRegistered(Checksum checksum);

	void registerCluster(Cluster cluster);
}
