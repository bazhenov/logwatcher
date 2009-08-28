package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class MySqlLogStorage implements LogStorage {

	private final Marshaller marshaller;
	private final SqlMatcherMapper mapper;
	private final SimpleJdbcTemplate jdbc;
	private final ParameterizedRowMapper<AggregatedLogEntry> entryCreator;
	private final Logger log = Logger.getLogger(MySqlLogStorage.class);

	public MySqlLogStorage(DataSource dataSource, Marshaller marshaller, SqlMatcherMapper mapper) {
		this.marshaller = marshaller;
		this.mapper = mapper;
		this.jdbc = new SimpleJdbcTemplate(dataSource);
		entryCreator = new EntryCreator(marshaller);
	}

	public void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			String sql = "INSERT INTO log_entry (`date`, `checksum`, `group`, `text`, `count`, `last_date`) "
				+ "VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `last_date` = ?, `count` = `count` + 1";

			DateTime date = entry.getDate();
			Object[] args = new Object[]{date(date.getDate()), entry.getChecksum(), entry.getGroup(),
				marshaller.marshall(entry), 1, timestamp(date), timestamp(date)};
			jdbc.update(sql, args);

			if ( log.isDebugEnabled() ) {
				log.debug("Entry with checksum: " + entry.getChecksum() + " wrote to database");
			}
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException {
		try {
			return jdbc.query("SELECT * FROM `log_entry` WHERE `date` = ?", entryCreator, date(date));
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public int getEntryCount(Date date) throws LogStorageException {
		try {
			return jdbc.queryForInt("SELECT SUM(`count`) FROM `log_entry` WHERE `date` = ?", date(date));
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		String sql = "SELECT COUNT(*) FROM `log_entry` l";
		List arguments = new LinkedList();
		StringBuilder whereClause = new StringBuilder();
		Collection<LogEntryMatcher> lateBoundMatchers = fillWhereClause(criterias, whereClause,
			arguments);
		if ( lateBoundMatchers.size() > 0 ) {
			throw new InvalidCriteriaException(lateBoundMatchers);
		}
		try {
			return jdbc.queryForInt(sql);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public void removeEntries(String checksum, Date date) throws LogStorageException {
		try {
			jdbc.update("DELETE FROM `log_entry` WHERE date = ? AND checksum = ?", date(date), checksum);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	/**
	 * Принимает критерии отбора (список обьектов типа {@link LogEntryMatcher}), буффер куда
	 * писать WHERE clause и список куда добавлять sql аргументы.
	 *
	 * @param criterias критерии отбора
	 * @param builder   буффер для записи выражения WHERE
	 * @param arguments список куда будут добавлены sql аргументы
	 * @return список критериев, которые не могут быть обработаны {@link SqlMatcherMapper}'ом
	 */
	private Collection<LogEntryMatcher> fillWhereClause(Collection<LogEntryMatcher> criterias,
	                                                    StringBuilder builder, List arguments) {
		WhereClause where = new WhereClause(builder, arguments);
		for ( LogEntryMatcher matcher : criterias ) {
			if ( mapper.handle(matcher, where) ) {
				criterias.remove(matcher);
			}
		}
		return criterias;
	}

	private java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	private Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	private static class EntryCreator implements ParameterizedRowMapper<AggregatedLogEntry> {

		private final Marshaller marshaller;

		public EntryCreator(Marshaller marshaller) {
			this.marshaller = marshaller;
		}

		public AggregatedLogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
			try {
				return createEntry(rs);
			} catch ( MarshallerException e ) {
				throw new SQLException(e);
			}
		}

		private AggregatedLogEntry createEntry(ResultSet rs) throws MarshallerException, SQLException {
			LogEntry sampleEntry = marshaller.unmarshall(rs.getString("text"));
			DateTime lastTime = new DateTime(rs.getTimestamp("last_date"));
			int count = rs.getInt("count");
			return new AggregatedLogEntryImpl(sampleEntry, lastTime, count);
		}
	}
}