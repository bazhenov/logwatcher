package org.bazhenov.logging.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.bazhenov.logging.marshalling.JDomMarshaller;
import org.bazhenov.logging.storage.sql.AnnotationDrivenMatcherMapperImpl;
import org.bazhenov.logging.storage.sql.SqlLogStorage;
import static org.bazhenov.logging.storage.sql.SqlLogStorage.loadDump;
import org.bazhenov.logging.storage.sql.SqlMatcherMapper;
import org.bazhenov.logging.storage.sql.SqlMatcherMapperRules;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class SqlLogStorageMySqlTest extends LogStorageTest {

	protected LogStorage createStorage() throws IOException, SQLException {
		String fileName = System.getProperty("mysql.config", "mysql.properties");
		Properties props = new Properties();
		try {
			props.load(new FileReader(fileName));
		} catch ( IOException e ) {
			throw new RuntimeException(
				"Unable to read MySQL properties file. Run tests with -Dmysql.config=../path/to/mysql.properties");
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

		InputStream stream = SqlLogStorage.class.getResourceAsStream("/dump.mysql.sql");
		loadDump(ds, stream);

		SqlMatcherMapper mapper = new AnnotationDrivenMatcherMapperImpl(new SqlMatcherMapperRules());
		return new SqlLogStorage(ds, new JDomMarshaller(), mapper);
	}
}
