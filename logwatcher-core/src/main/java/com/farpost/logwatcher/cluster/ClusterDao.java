package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;

public interface ClusterDao {

	boolean isClusterRegistered(String applicationId, Checksum checksum);

	void registerCluster(Cluster cluster);

	Cluster findCluster(String applicationId, Checksum checksum);
}
