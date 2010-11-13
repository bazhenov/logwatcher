package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.marshalling.Marshaller;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class CreateEntryRowMapper implements ParameterizedRowMapper<LogEntry> {

	private final Marshaller marshaller;

	public CreateEntryRowMapper(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
		return marshaller.unmarshall(rs.getString("content"));
	}
}