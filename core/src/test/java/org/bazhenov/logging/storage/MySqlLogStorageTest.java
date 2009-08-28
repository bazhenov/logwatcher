package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.marshalling.JDomMarshaller;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class MySqlLogStorageTest extends LogStorageTest {

	protected LogStorage createStorage() throws IOException, SQLException {
		String fileName = System.getProperty("mysql.config", "mysql.properties");
		Properties props = new Properties();
		try {
			props.load(new FileReader(fileName));
		} catch ( IOException e ) {
			throw new RuntimeException(
				"Unable to read mysql properties file. Run tests with -Dmysql.config=../path/to/mysql.properties");
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

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(MySqlLogStorage.class.getResourceAsStream("/dump.sql")));
		String line;
		while ( (line = reader.readLine()) != null ) {
			buffer.append(line).append("\n");
		}

		Connection connection = ds.getConnection();
		try {
			connection.prepareStatement(buffer.toString()).executeUpdate();
		} finally {
			connection.close();
		}

		return new MySqlLogStorage(ds, new JDomMarshaller(), new AnnotationDrivenMatcherMapperImpl(new Object()));
	}
}
