package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
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