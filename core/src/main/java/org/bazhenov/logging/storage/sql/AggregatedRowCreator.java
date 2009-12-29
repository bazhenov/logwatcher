package org.bazhenov.logging.storage.sql;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedEntry;
import org.bazhenov.logging.AggregatedEntryImpl;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class AggregatedRowCreator implements ParameterizedRowMapper<AggregatedEntry> {

	private final Marshaller marshaller;

	public AggregatedRowCreator(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public AggregatedEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			LogEntry entry = marshaller.unmarshall(rs.getString("content"));
			return new AggregatedEntryImpl(entry.getMessage(), rs.getString("checksum"),
				rs.getInt("count"), new DateTime(rs.getTimestamp("last_time").getTime()), entry.getCause());
		} catch ( MarshallerException e ) {
			throw new RuntimeException(e);
		}
	}
}
