package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class MySqlLogStorage implements LogStorage {

	private final Marshaller marshaller;
	private final SimpleJdbcTemplate jdbc;
	private final ParameterizedRowMapper<AggregatedLogEntry> entryCreator;

	public MySqlLogStorage(DataSource dataSource, Marshaller marshaller) {
		this.marshaller = marshaller;
		this.jdbc = new SimpleJdbcTemplate(dataSource);

		entryCreator = new ParameterizedRowMapper<AggregatedLogEntry>() {
			public AggregatedLogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
				try {
					return createEntry(rs);
				} catch ( MarshallerException e ) {
					throw new SQLException(e);
				}
			}
		};
	}

	public void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			String sql = "INSERT INTO log_entry (`date`, `checksum`, `group`, `text`, `count`, `last_date`) "	+
				"VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `last_date` = ?, `count` = `count` + 1";

			DateTime date = entry.getDate();
			Object []args = new Object[] {date(date.getDate()), entry.getChecksum(), entry.getGroup(),
				marshaller.marshall(entry),	1, timestamp(date), timestamp(date)	};
			jdbc.update(sql, args);
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException {
		return jdbc.query("SELECT * FROM `log_entry` WHERE `date` = ?", entryCreator, date(date));
	}

	public int getEntryCount(Date date) throws LogStorageException {
		return jdbc.queryForInt("SELECT SUM(`count`) FROM `log_entry` WHERE `date` = ?", date(date));
	}

	public int countEntries(Collection<LogEntryMatcher> criterias) throws LogStorageException {
		return jdbc.queryForInt("SELECT COUNT(*) FROM `log_entry`");
	}

	private java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	private Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	private AggregatedLogEntry createEntry(ResultSet rs) throws MarshallerException, SQLException {
		LogEntry sampleEntry = marshaller.unmarshall(rs.getString("text"));
		DateTime lastTime = new DateTime(rs.getTimestamp("last_date"));
		int count = rs.getInt("count");
		return new AggregatedLogEntryImpl(sampleEntry, lastTime, count);
	}
}
