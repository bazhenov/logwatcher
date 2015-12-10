package com.farpost.logwatcher;

import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.google.common.base.Charsets;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.MIN_VALUE;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

public class Reconvert {

	private static final Charset UTF_8 = Charset.forName("utf8");

	public static void main(String[] args) throws IOException, SQLException {
		Marshaller m = new Jaxb2Marshaller();

		DataSource ds = new SingleConnectionDataSource("jdbc:mysql://192.168.2.10/test", "root", "", true);

		SimpleChecksumCalculator c = new SimpleChecksumCalculator();

		Map<String, List<LogEntry>> entries = new HashMap<>();
		try(Connection connection = ds.getConnection();
		    Statement s = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)) {
			s.setFetchSize(MIN_VALUE);
			ResultSet rs = s.executeQuery("SELECT value FROM entry");

			while(rs.next()) {
				String xml = rs.getString(1);
				LogEntry e = m.unmarshall(xml.getBytes(UTF_8));
				if(e.getApplicationId().equalsIgnoreCase("search")) {

					String checksum = c.calculateChecksum(e);
					if(!entries.containsKey(checksum)) {
						entries.put(checksum, new ArrayList<>());
					}
					entries.get(checksum).add(e);

				}
			}
		}

		try(Writer writer = new OutputStreamWriter(new FileOutputStream("result.log"), Charsets.UTF_8)) {
			for(Map.Entry<String, List<LogEntry>> mapEntry : entries.entrySet()) {
				writer.write("Checksum: " + mapEntry.getKey() + "\n");
				for(LogEntry e : mapEntry.getValue()) {
					String cause = e.getCause() != null ? "/" + e.getCause().getType() : "";
					writer.write(e.getSeverity() + " [" + e.getGroup() + "]" + cause + ": " + e.getMessage() + "\n");
				}
				writer.write("\n");
			}
		}
	}
}
