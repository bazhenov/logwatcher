package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;

import java.util.function.Consumer;

public interface ClusterDao {

	void registerCluster(Cluster cluster);

	Cluster findCluster(String applicationId, Checksum checksum);

	/**
	 * Change cluster using given function
	 *
	 * @param applicationId application id of the cluster
	 * @param checksum      cluster checksum
	 * @param f             function which is change cluster
	 */
	void changeCluster(String applicationId, Checksum checksum, Consumer<Cluster> f);
}
