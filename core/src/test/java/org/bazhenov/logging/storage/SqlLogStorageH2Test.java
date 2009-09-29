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
		String fileName = System.getProperty("h2.config", "h2.properties");
		Properties props = new Properties();
		try {
			props.load(new FileReader(fileName));
		} catch ( IOException e ) {
			throw new RuntimeException(
				"Unable to read H2 properties file. Run tests with -Dh2.config=../path/to/h2.properties");
		}
		String url = props.getProperty("url");
		String username = props.getProperty("username");
		String password = props.getProperty("password");
		String driver = props.getProperty("driver");

		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(driver);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);

		InputStream stream = SqlLogStorage.class.getResourceAsStream("/dump.h2.sql");
		loadDump(ds, stream);

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		return new SqlLogStorage(ds, new JDomMarshaller(), mapper);
	}
}