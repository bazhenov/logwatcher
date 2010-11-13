package com.farpost.logwatcher.storage;

import com.farpost.logging.marshalling.Jaxb2Marshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logwatcher.SimpleChecksumCalculator;
import com.farpost.logwatcher.aggregator.Aggregator;
import com.farpost.logwatcher.aggregator.SimpleAggregator;
import com.farpost.logwatcher.storage.sql.AnnotationDrivenMatcherMapperImpl;
import com.farpost.logwatcher.storage.sql.SqlLogStorage;
import com.farpost.logwatcher.storage.sql.SqlMatcherMapper;
import com.farpost.logwatcher.storage.sql.SqlMatcherMapperRules;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.testng.annotations.AfterMethod;

import java.io.IOException;
import java.sql.SQLException;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

public class SqlLogStorageH2Test extends LogStorageTestCase {
	private EmbeddedDatabase db;

	protected LogStorage createStorage() throws IOException, SQLException {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		db = builder.
			setType(H2).
			addScript("schema.sql").
			build();

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		Marshaller marshaller = new Jaxb2Marshaller();
		Aggregator aggregator = new SimpleAggregator(marshaller);
		return new SqlLogStorage(aggregator, db, marshaller, mapper, new SimpleChecksumCalculator());
	}

	@AfterMethod
	protected void tearDown() {
		db.shutdown();
	}
}