package com.farpost.logwatcher.cluster;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.testng.annotations.AfterMethod;

public class SqlClusterDaoTest extends ClusterDaoTest {

	private EmbeddedDatabase db;

	@AfterMethod
	public void tearDown() {
		db.shutdown();
	}

	@Override
	protected ClusterDao createDao() {
		db = new EmbeddedDatabaseBuilder().
			setType(EmbeddedDatabaseType.H2).
			addScript("classpath:com/farpost/logwatcher/storage/schema.sql").
			build();
		return new SqlClusterDao(db);
	}
}
