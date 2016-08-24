package com.farpost.logwatcher.storage;

import org.apache.lucene.store.RAMDirectory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LuceneSqlLogStorageMySqlIT extends LogStorageTestCase {

	private SingleConnectionDataSource ds;

	@BeforeClass
	protected void setUpDataSource() throws Exception {
		Properties properties = getProperties();
		String url = properties.getProperty("jdbc.url");
		String user = properties.getProperty("jdbc.user");
		String password = properties.getProperty("jdbc.password");
		ds = new SingleConnectionDataSource(url, user, password, true);

		execSql(
			"/com/farpost/logwatcher/storage/schema-cleanup.sql",
			"/com/farpost/logwatcher/storage/schema.sql"
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

	/**
	 * Возвращает список свойств, необходимых для подключения к MySql-бд.
	 * <p/>
	 * Если в classpath присутсвует файл mysql.properties, то значения достаются из него,
	 * в ином случае используются System-properties
	 *
	 * @return свойства (@link Properties}, необходимые для подключения к бд
	 * @throws IOException Ошибка заполнение свойств из найденного(!) файла mysql.properties
	 */
	private Properties getProperties() throws IOException {
		Properties properties;
		InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream("mysql.properties");
		if (propertiesInputStream != null) {
			properties = new Properties();
			properties.load(propertiesInputStream);
		} else {
			properties = System.getProperties();
		}

		if (properties.getProperty("jdbc.url") == null) {
			throw new AssertionError("You should set 'jdbc.url' property in mysql.properties file (if it exists) or in the system properties");
		}

		return properties;
	}
}
