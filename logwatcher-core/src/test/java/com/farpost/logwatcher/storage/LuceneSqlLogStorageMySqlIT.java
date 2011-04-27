package com.farpost.logwatcher.storage;

import org.apache.lucene.store.RAMDirectory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import javax.sql.DataSource;

import java.util.Properties;

import static java.lang.System.getProperty;

public class LuceneSqlLogStorageMySqlIT extends LogStorageTestCase {

	private SingleConnectionDataSource ds;

	@BeforeClass
	protected void setUpDataSource() throws Exception {
		String url = getProperty("jdbc.url");
		String user = getProperty("jdbc.user");
		String password = getProperty("jdbc.password");
		ds = new SingleConnectionDataSource(url, user, password, true);

		execSql(
			"/com/farpost/logwatcher/storage/schema-cleanup.sql",
			"/com/farpost/logwatcher/storage/schema.sql",
			"/com/farpost/logwatcher/storage/schema-indexes.sql"
		);
	}

	@Override
	protected LogStorage createStorage() throws Exception {
		execSql("/com/farpost/logwatcher/storage/schema-truncate.sql");
		LuceneSqlLogStorage storage = new LuceneSqlLogStorage(new RAMDirectory(), ds);
		storage.setCommitThreshold(0);
		return storage;
	}

	private void execSql(String... resources) throws Exception {
		DataSourceInitializer initializer = new DataSourceInitializer();
		ResourceDatabasePopulator r = new ResourceDatabasePopulator();
		for (String resource : resources) {
			r.addScript(new ClassPathResource(resource));
		}
		initializer.setDatabasePopulator(r);
		initializer.setDataSource(ds);

		initializer.afterPropertiesSet();
	}
}
