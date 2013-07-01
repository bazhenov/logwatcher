package com.farpost.logwatcher.statistics;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.testng.annotations.AfterMethod;

public class SqlClusterStatisticImplTest extends ClusterStatisticsTest {

	private EmbeddedDatabase db;

	@AfterMethod
	public void tearDown() {
		db.shutdown();
	}

	@Override
	protected ClusterStatistic createClusterStatistic() {
		db = new EmbeddedDatabaseBuilder().
			setType(EmbeddedDatabaseType.H2).
			addScript("classpath:com/farpost/logwatcher/storage/schema.sql").
			build();
		return new SqlClusterStatisticImpl(db);
	}
}
