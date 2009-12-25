package org.bazhenov.logging.storage;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSchema {

	private final InputStream initDump;
	private final InputStream cleanupDump;

	public DatabaseSchema(InputStream initDump, InputStream cleanupDump) {
		this.initDump = initDump;
		this.cleanupDump = cleanupDump;
	}

	public void init(DataSource dataSource) throws IOException, SQLException {
		exec(dataSource, initDump);
	}

	public void cleanup(DataSource dataSource) throws IOException, SQLException {
		exec(dataSource, cleanupDump);
	}

	public static void exec(DataSource ds, InputStream stream) throws IOException, SQLException {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
	}
}
