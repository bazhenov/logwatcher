package com.farpost.logwatcher.cluster;

public class InMemoryClusterDaoTest extends ClusterDaoTestCase {

	@Override
	protected ClusterDao createDao() {
		return new InMemoryClusterDao();
	}
}
