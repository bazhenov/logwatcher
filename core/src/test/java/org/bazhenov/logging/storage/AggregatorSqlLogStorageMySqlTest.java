package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.aggregator.SimpleAggregator;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.AggregatorSqlLogStorage;
import org.bazhenov.logging.storage.sql.AnnotationDrivenMatcherMapperImpl;
import org.bazhenov.logging.storage.sql.SqlMatcherMapper;
import org.bazhenov.logging.storage.sql.SqlMatcherMapperRules;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class AggregatorSqlLogStorageMySqlTest extends LogStorageTestCase {

	protected LogStorage createStorage() throws IOException, SQLException {
		String pathToConfig = System.getProperty("mysql.config", "./mysql.properties");
		Properties props = new Properties();
		props.load(new FileReader(pathToConfig));
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(props.getProperty("driver"));
		ds.setUsername(props.getProperty("username"));
		ds.setPassword(props.getProperty("password"));
		ds.setUrl(props.getProperty("url"));

		InputStream initDump = DatabaseSchema.class.getResourceAsStream("/dump-init.mysql.sql");
		InputStream cleanupDump = DatabaseSchema.class.getResourceAsStream("/dump-cleanup.mysql.sql");
		DatabaseSchema schema = new DatabaseSchema(initDump, cleanupDump);
		schema.cleanup(ds);
		schema.init(ds);

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		JDomMarshaller marshaller = new JDomMarshaller();
		Aggregator aggregator = new SimpleAggregator(marshaller);
		return new AggregatorSqlLogStorage(aggregator, ds, marshaller, mapper);
	}
}