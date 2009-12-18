package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.aggregator.SimpleAggregator;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.*;

import java.io.IOException;
import java.sql.SQLException;

public class AggregatorSqlLogStorageH2Test extends LogStorageTest {

	protected LogStorage createStorage() throws IOException, SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setUrl("jdbc:h2:mem:");

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		Aggregator aggregator = new SimpleAggregator();
		return new AggregatorSqlLogStorage(aggregator, ds, new JDomMarshaller(), mapper);
	}
}