package org.bazhenov.logging.storage.sql;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import com.farpost.marshaller.DomMarshallerImpl;
import com.farpost.marshaller.MarshallingException;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.AggregatedLogEntryImpl.*;
import org.bazhenov.logging.storage.*;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.io.*;

public class SqlLogStorage implements LogStorage {

	private final Marshaller marshaller;
	private final SqlMatcherMapper mapper;
	private final SimpleJdbcTemplate jdbc;
	private final ParameterizedRowMapper<AggregatedLogEntry> entryCreator;
	private final com.farpost.marshaller.Marshaller attributesMarshaller;
	private final Logger log = Logger.getLogger(SqlLogStorage.class);

	public SqlLogStorage(DataSource dataSource, Marshaller marshaller, SqlMatcherMapper mapper) throws
		IOException, SQLException {
		this.marshaller = marshaller;
		this.mapper = mapper;
		this.jdbc = new SimpleJdbcTemplate(dataSource);
		entryCreator = new EntryCreator();
		attributesMarshaller = new DomMarshallerImpl();

		InputStream stream = SqlLogStorage.class.getResourceAsStream("/dump.h2.sql");
		loadDump(dataSource, stream);
	}

	public synchronized void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			String marshalledEntry = marshaller.marshall(entry);
			int rowsUpdated = jdbc.update(
				"UPDATE log_entry SET count = count + 1, last_date = ? WHERE date = ? AND checksum = ?",
				timestamp(entry.getDate()), date(entry.getDate()), entry.getChecksum());
			if ( rowsUpdated <= 0 ) {
				String sql = "INSERT INTO log_entry (date, checksum, category, text, count, last_date, application_id, severity, attributes) " + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

				DateTime date = entry.getDate();

				Map<String, Map<String, Integer>> aggregatedAttributes = new HashMap<String, Map<String, Integer>>();
				mergeMap(aggregatedAttributes, entry.getAttributes());


				Object[] args = new Object[]{date(date.getDate()), entry.getChecksum(), entry.getCategory(),
					marshalledEntry, 1, timestamp(date), entry.getApplicationId(),
					entry.getSeverity().getCode(),
					attributesMarshaller.serialize((Map)aggregatedAttributes)};
				jdbc.update(sql, args);
			} else {
				String xml = jdbc.queryForObject(
					"SELECT attributes FROM log_entry WHERE date = ? AND checksum = ?", String.class,
					date(entry.getDate()), entry.getChecksum());
				Map<String, Object> map = xml != null
					? attributesMarshaller.unserialize(new StringReader(xml))
					: new HashMap<String, Object>();
				mergeMap((Map) map, entry.getAttributes());
				jdbc.update("UPDATE log_entry SET attributes = ? WHERE date = ? AND checksum = ?",
					attributesMarshaller.serialize(map), date(entry.getDate()), entry.getChecksum());
			}

			/**
			 * Пишем запись в новом формате без аггрегации
			 */
			jdbc.update("INSERT INTO entry (time, date, checksum, category, severity, application_id, content) VALUES (?, ?, ?, ?, ?, ?, ?)",
				timestamp(entry.getDate()), date(entry.getDate()), entry.getChecksum(), entry.getCategory(),
				entry.getSeverity().getCode(), entry.getApplicationId(), marshalledEntry);

			if ( log.isDebugEnabled() ) {
				log.debug("Entry with checksum: " + entry.getChecksum() + " wrote to database");
			}
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		} catch ( MarshallingException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<AggregatedLogEntry> getEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		StringBuilder sql = new StringBuilder("SELECT * FROM `log_entry` l");
		if ( criterias == null || criterias.size() <= 0 ) {
			throw new InvalidCriteriaException("Empty criteria given");
		} else {
			List arguments = new LinkedList();
			StringBuilder whereClause = new StringBuilder();
			try {
				Collection<LogEntryMatcher> lateBoundMatchers = fillWhereClause(criterias, whereClause,
					arguments);
				if ( lateBoundMatchers.size() > 0 ) {
					throw new InvalidCriteriaException(lateBoundMatchers);
				}
				sql.append(" WHERE ").append(whereClause).append(" ORDER BY l.last_date DESC");
				return jdbc.query(sql.toString(), entryCreator, arguments.toArray());
			} catch ( DataAccessException e ) {
				throw new LogStorageException(e);
			} catch ( MatcherMapperException e ) {
				throw new InvalidCriteriaException(e);
			}
		}
	}

	public void createChecksumAlias(String checksum, String alias) {
		jdbc.update("DELETE FROM log_entry WHERE checksum = ?", checksum);
	}

	public int getEntryCount(Date date) throws LogStorageException {
		try {
			return jdbc.queryForInt("SELECT SUM(count) FROM log_entry WHERE date = ?", date(date));
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `log_entry` l");
		if ( criterias == null || criterias.size() <= 0 ) {
			return jdbc.queryForInt(sql.toString());
		} else {
			List arguments = new LinkedList();
			StringBuilder whereClause = new StringBuilder();
			try {
				Collection<LogEntryMatcher> lateBoundMatchers = fillWhereClause(criterias, whereClause,
					arguments);
				if ( lateBoundMatchers.size() > 0 ) {
					throw new InvalidCriteriaException(lateBoundMatchers);
				}
				sql.append(" WHERE ").append(whereClause);
				return jdbc.queryForInt(sql.toString(), arguments.toArray());
			} catch ( DataAccessException e ) {
				throw new LogStorageException(e);
			} catch ( MatcherMapperException e ) {
				throw new InvalidCriteriaException(e);
			}
		}
	}

	public void removeEntries(String checksum, Date date) throws LogStorageException {
		try {
			jdbc.update("DELETE FROM log_entry WHERE date = ? AND checksum = ?", date(date), checksum);
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
	                                                    StringBuilder builder, List arguments)
		throws MatcherMapperException {

		WhereClause where = new WhereClause(builder, arguments);
		Iterator<LogEntryMatcher> iterator = criterias.iterator();
		while ( iterator.hasNext() ) {
			LogEntryMatcher matcher = iterator.next();
			if ( mapper.handle(matcher, where) ) {
				iterator.remove();
			}
		}
		return criterias;
	}

	static java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	public static void loadDump(DataSource ds, InputStream stream) throws IOException, SQLException {
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

	static Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	/**
	 * Имплементация {@link ParameterizedRowMapper}, которая из {@link ResultSet}'а создает
	 * обьекты типа {@link AggregatedLogEntry}.
	 */
	private class EntryCreator implements ParameterizedRowMapper<AggregatedLogEntry> {

		public AggregatedLogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
			try {
				return createEntry(rs);
			} catch ( MarshallerException e ) {
				throw new SQLException(e);
			} catch ( MarshallingException e ) {
				throw new SQLException(e);
			}
		}

		private AggregatedLogEntry createEntry(ResultSet rs) throws MarshallerException, SQLException,
			MarshallingException {
			LogEntry sampleEntry = marshaller.unmarshall(rs.getString("text"));
			String attributesStr = rs.getString("attributes");
			Map<String, Object> attributes = attributesStr != null
				? attributesMarshaller.unserialize(new StringReader(attributesStr))
				: new HashMap<String, Object>();
			for ( Map.Entry<String, Object> row : attributes.entrySet() ) {
				Map aggregatedAttributes = (Map) row.getValue();
				attributes.put(row.getKey(), new AggregatedAttribute(row.getKey(), aggregatedAttributes));
			}
			DateTime lastTime = new DateTime(rs.getTimestamp("last_date"));
			int count = rs.getInt("count");
			return new AggregatedLogEntryImpl(sampleEntry, lastTime, count, (Map)attributes);
		}
	}
}
