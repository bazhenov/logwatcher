package com.farpost.logwatcher.storage;

import org.apache.lucene.store.RAMDirectory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.testng.annotations.AfterMethod;

public class LuceneSqlLogStorageH2Test extends LogStorageTestCase {

	private EmbeddedDatabase db;

	@AfterMethod
	public void shutdownDatabase() {
		db.shutdown();
	}

	@Override
	protected LogStorage createStorage() throws Exception {
		db = new EmbeddedDatabaseBuilder().
			setType(EmbeddedDatabaseType.H2).
			addScript("classpath:com/farpost/logwatcher/storage/schema.sql").
			build();

		LuceneSqlLogStorage storage = new LuceneSqlLogStorage(new RAMDirectory(), db);
		storage.setCommitThreshold(0);
		return storage;
	}
}
