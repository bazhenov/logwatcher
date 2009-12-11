package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.AnnotationDrivenMatcherMapperImpl;
import org.bazhenov.logging.storage.sql.SqlLogStorage;
import org.bazhenov.logging.storage.sql.SqlMatcherMapper;
import org.bazhenov.logging.storage.sql.SqlMatcherMapperRules;

import java.io.IOException;
import java.sql.SQLException;

public class SqlLogStorageH2Test extends LogStorageTest {

	protected LogStorage createStorage() throws IOException, SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setUrl("jdbc:h2:mem:");

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		return new SqlLogStorage(ds, new JDomMarshaller(), mapper);
	}
}
