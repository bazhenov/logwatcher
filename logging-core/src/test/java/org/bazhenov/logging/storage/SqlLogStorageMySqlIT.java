package org.bazhenov.logging.storage;

import com.farpost.logging.marshalling.Jaxb2Marshaller;
import com.farpost.logging.marshalling.Marshaller;
import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.SimpleChecksumCalculator;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.aggregator.SimpleAggregator;
import org.bazhenov.logging.storage.sql.AnnotationDrivenMatcherMapperImpl;
import org.bazhenov.logging.storage.sql.SqlLogStorage;
import org.bazhenov.logging.storage.sql.SqlMatcherMapper;
import org.bazhenov.logging.storage.sql.SqlMatcherMapperRules;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SqlLogStorageMySqlIT extends LogStorageTestCase {

	private DataSource ds;

	@BeforeClass
	public void prepareDatasource() throws IOException, SQLException {
		String pathToConfig = System.getProperty("mysql.config", "./mysql.properties");
		Properties props = new Properties();
		props.load(new FileReader(pathToConfig));
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(props.getProperty("driver"));
		ds.setUsername(props.getProperty("username"));
		ds.setPassword(props.getProperty("password"));
		ds.setUrl(props.getProperty("url"));
		this.ds = ds;

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("schema-cleanup.sql"));
		populator.addScript(new ClassPathResource("schema.sql"));
		Connection connection = ds.getConnection();
		populator.populate(connection);
		connection.close();
	}

	@BeforeMethod
	public void truncateData() throws SQLException {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("schema-truncate.sql"));
		Connection connection = ds.getConnection();
		populator.populate(connection);
		connection.close();
	}

	protected LogStorage createStorage() throws IOException, SQLException {
		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		Marshaller marshaller = new Jaxb2Marshaller();
		Aggregator aggregator = new SimpleAggregator(marshaller);
		return new SqlLogStorage(aggregator, ds, marshaller, mapper, new SimpleChecksumCalculator());
	}
}