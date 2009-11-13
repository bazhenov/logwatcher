package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.*;
import static org.bazhenov.logging.storage.sql.SqlLogStorage.loadDump;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class SqlLogStorageH2Test extends LogStorageTest {

	protected LogStorage createStorage() throws IOException, SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setUrl("jdbc:h2:./test/as");

		InputStream stream = SqlLogStorage.class.getResourceAsStream("/dump.h2.sql");
		loadDump(ds, stream);

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		return new SqlLogStorage(ds, new JDomMarshaller(), mapper);
	}
}