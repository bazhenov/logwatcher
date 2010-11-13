package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.AggregatedEntryImpl;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.marshalling.MarshallerException;
import com.farpost.timepoint.DateTime;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class CreateAggregatedEntryRowMapper implements ParameterizedRowMapper<AggregatedEntry> {

	private final Marshaller marshaller;

	public CreateAggregatedEntryRowMapper(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public AggregatedEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
		LogEntry entry = marshaller.unmarshall(rs.getString("content"));
		return new AggregatedEntryImpl(entry.getMessage(), rs.getString("checksum"),
			rs.getString("application_id"), Severity.forCode(rs.getInt("severity")), rs.getInt("count"),
			new DateTime(rs.getTimestamp("last_time").getTime()), entry.getCause());
	}
}
