package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Cluster;

import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class InMemoryClusterDao implements ClusterDao {

	private final Map<Checksum, Cluster> clusterMap = newHashMap();

	@Override
	public boolean isClusterRegistered(byte[] checksum) {
		return clusterMap.containsKey(new Checksum(checksum));
	}

	@Override
	public synchronized void registerCluster(Cluster cluster) {
		checkArgument(!clusterMap.containsKey(cluster.getChecksum()), "Cluster already registered");
		clusterMap.put(new Checksum(cluster.getChecksum()), cluster);
	}
}

/**
 * Wrapper for byte array to enable by-value equality in collection library.
 */
final class Checksum {

	private final byte[] checksum;

	Checksum(byte[] checksum) {
		this.checksum = checkNotNull(checksum);
		checkArgument(checksum.length > 0, "Should be non empty array");
	}

	byte[] asByteArray() {
		return checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Checksum)) return false;

		Checksum checksum1 = (Checksum) o;

		return Arrays.equals(checksum, checksum1.checksum);

	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(checksum);
	}
}