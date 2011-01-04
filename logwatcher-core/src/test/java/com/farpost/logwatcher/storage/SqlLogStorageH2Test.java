package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.SimpleChecksumCalculator;
import com.farpost.logwatcher.aggregator.Aggregator;
import com.farpost.logwatcher.aggregator.SimpleAggregator;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.sql.SqlLogStorage;
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

		Marshaller marshaller = new Jaxb2Marshaller();
		Aggregator aggregator = new SimpleAggregator(marshaller);
		return new SqlLogStorage(aggregator, db, marshaller, new SimpleChecksumCalculator());
	}

	@AfterMethod
	protected void tearDown() {
		db.shutdown();
	}
}