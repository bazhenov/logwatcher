package com.farpost.logwatcher.statistics;

public class InMemoryClusterStatisticsTest extends ClusterStatisticsTest {

	@Override
	protected ClusterStatistic createClusterStatistic() {
		return new InMemoryClusterStatistics();
	}
}
