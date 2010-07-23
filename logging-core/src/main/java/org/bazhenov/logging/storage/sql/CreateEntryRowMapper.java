package org.bazhenov.logging.storage.sql;

import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import org.bazhenov.logging.LogEntry;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class CreateEntryRowMapper implements ParameterizedRowMapper<LogEntry> {

	private final Marshaller marshaller;

	public CreateEntryRowMapper(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			return marshaller.unmarshall(rs.getString("content"));
		} catch ( MarshallerException e ) {
			throw new RuntimeException(e);
		}
	}
}