package com.farpost.logwatcher.statistics;

public class InMemoryClusterStatisticsTest extends ClusterStatisticsTestCase {

	@Override
	protected ClusterStatistic createClusterStatistic() {
		return new InMemoryClusterStatistics();
	}
}
