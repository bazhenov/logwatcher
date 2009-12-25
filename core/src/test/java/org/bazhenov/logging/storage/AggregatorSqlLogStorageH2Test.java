package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.aggregator.SimpleAggregator;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class AggregatorSqlLogStorageH2Test extends LogStorageTestCase {

	protected LogStorage createStorage() throws IOException, SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setUrl("jdbc:h2:mem:");

		InputStream initDump = DatabaseSchema.class.getResourceAsStream("/dump-init.h2.sql");
		InputStream cleanupDump = DatabaseSchema.class.getResourceAsStream("/dump-cleanup.h2.sql");
		DatabaseSchema schema = new DatabaseSchema(initDump, cleanupDump);
		schema.cleanup(ds);
		schema.init(ds);

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		JDomMarshaller marshaller = new JDomMarshaller();
		Aggregator aggregator = new SimpleAggregator(marshaller);
		return new AggregatorSqlLogStorage(aggregator, ds, marshaller, mapper);
	}
}