package com.farpost.logwatcher.cluster;

public class InMemoryClusterDaoTest extends ClusterDaoTest {

	@Override
	protected ClusterDao createDao() {
		return new InMemoryClusterDao();
	}
}
