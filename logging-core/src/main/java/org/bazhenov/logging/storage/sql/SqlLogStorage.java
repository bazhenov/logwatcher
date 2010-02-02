package org.bazhenov.logging.storage.sql;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.apache.log4j.Logger;
import org.bazhenov.logging.*;
import org.bazhenov.logging.aggregator.Aggregator;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import org.bazhenov.logging.storage.LogEntryMatcher;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.util.Collections.sort;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

public class SqlLogStorage implements LogStorage {

	private final Aggregator aggregator;
	private final DataSource datasource;
	private final Marshaller marshaller;
	private final SqlMatcherMapper mapper;
	private final SimpleJdbcTemplate jdbc;
	private final Logger log = Logger.getLogger(SqlLogStorage.class);
	private final ParameterizedRowMapper<AggregatedEntry> aggregateEntryMapper;
	private final ParameterizedRowMapper<LogEntry> entryMapper;

	public SqlLogStorage(Aggregator aggregator, DataSource datasource, Marshaller marshaller,
	                     SqlMatcherMapper mapper) throws IOException, SQLException {
		this.aggregator = aggregator;
		this.marshaller = marshaller;
		this.mapper = mapper;
		this.datasource = datasource;
		this.jdbc = new SimpleJdbcTemplate(datasource);
		this.aggregateEntryMapper = new CreateAggregatedEntryRowMapper(marshaller);
		this.entryMapper = new CreateEntryRowMapper(marshaller);
	}

	public synchronized void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			String marshalledEntry = marshaller.marshall(entry);
			Timestamp entryTimestamp = timestamp(entry.getDate());
			java.sql.Date entryDate = date(entry.getDate());
			jdbc.update(
				"INSERT INTO entry (time, date, checksum, category, severity, application_id, content) VALUES (?, ?, ?, ?, ?, ?, ?)",
				entryTimestamp, entryDate, entry.getChecksum(), entry.getCategory(),
				entry.getSeverity().getCode(), entry.getApplicationId(), marshalledEntry);

			int affectedRows = jdbc.update(
				"UPDATE aggregated_entry SET count = count + 1, last_time = IF(last_time < ?, ?, last_time) WHERE date = ? AND checksum = ?",
				entryTimestamp, entryTimestamp, entryDate, entry.getChecksum());
			if ( affectedRows == 0 ) {
				jdbc.update(
					"INSERT INTO aggregated_entry (date, checksum, last_time, category, severity, application_id, count, content) VALUES (?, ?, ?, ?, ?, ?, 1, ?)",
					entryDate, entry.getChecksum(), entryTimestamp, entry.getCategory(),
					entry.getSeverity().getCode(), entry.getApplicationId(), marshalledEntry);
			}

			if ( log.isDebugEnabled() ) {
				log.debug("Entry with checksum: " + entry.getChecksum() + " wrote to database");
			}
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		try {
			CriteriaStatement st = fillWhereClause(criterias);
			if ( st.haveLateBoundMatchers() ) {
				throw new InvalidCriteriaException(st.getLateBoundMatchers());
			}
			String sql = "SELECT content FROM entry l WHERE " + st.getWhereClause();
			return jdbc.query(sql, entryMapper, st.getArguments());
		} catch ( MatcherMapperException e ) {
			throw new LogStorageException(e);
		}
	}

	public List<AggregatedEntry> findAggregatedEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			StringBuilder sql = new StringBuilder("SELECT content FROM `entry` l");
			CriteriaStatement st = fillWhereClause(criterias);

			sql.append(" WHERE ").append(st.getWhereClause());
			connection = datasource.getConnection();
			statement = connection.prepareStatement(sql.toString(), TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);
			fill(statement, st.getArguments());

			result = statement.executeQuery();
			Collection<AggregatedEntry> aggregated = aggregator.aggregate(new ResultSetIterable(result),
				st.getLateBoundMatchers());
			return new ArrayList<AggregatedEntry>(aggregated);
		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		} catch ( MatcherMapperException e ) {
			throw new LogStorageException(e);
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} finally {
			close(result);
			close(statement);
			close(connection);
		}
	}

	public void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			StringBuilder sql = new StringBuilder("SELECT content FROM `entry` l");

			CriteriaStatement st = fillWhereClause(criterias);

			sql.append(" WHERE ").append(st.getWhereClause());
			connection = datasource.getConnection();
			statement = connection.prepareStatement(sql.toString(), TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);
			fill(statement, st.getArguments());

			result = statement.executeQuery();
			Collection<LogEntryMatcher> lateBoundMatchers = st.getLateBoundMatchers();
			while ( result.next() ) {
				LogEntry entry = marshaller.unmarshall(result.getString("content"));
				if ( isMatching(entry, lateBoundMatchers) ) {
					visitor.visit(entry);
				}
			}
		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		} catch ( MatcherMapperException e ) {
			throw new LogStorageException(e);
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} finally {
			close(result);
			close(statement);
			close(connection);
		}
	}

	public List<AggregatedEntry> getAggregatedEntries(Date date, Severity severity) {
		return jdbc.query(
			"SELECT checksum, application_id, last_time, count, severity, content FROM aggregated_entry WHERE date = ? AND severity >= ?",
			aggregateEntryMapper, date(date), severity.getCode());
	}

	private void fill(PreparedStatement statement, Object[] arguments) throws SQLException {
		int i = 1;
		for ( Object obj : arguments ) {
			if ( obj instanceof String ) {
				statement.setString(i++, (String) obj);
			} else if ( obj instanceof java.sql.Date ) {
				statement.setDate(i++, (java.sql.Date) obj);
			} else if ( obj instanceof Timestamp ) {
				statement.setTimestamp(i++, (Timestamp) obj);
			} else if ( obj instanceof Integer ) {
				statement.setInt(i++, (Integer) obj);
			} else {
				throw new RuntimeException("Unknown argument type: " + obj.getClass().getName());
			}
		}
	}

	public void createChecksumAlias(String checksum, String alias) {
		jdbc.update("UPDATE entry SET checksum = ? WHERE checksum = ?", alias, checksum);
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `entry` l");
		if ( criterias == null || criterias.size() <= 0 ) {
			return jdbc.queryForInt(sql.toString());
		} else {
			try {
				CriteriaStatement st = fillWhereClause(criterias);
				if ( st.haveLateBoundMatchers() ) {
					throw new InvalidCriteriaException(st.getLateBoundMatchers());
				}
				sql.append(" WHERE ").append(st.getWhereClause());
				return jdbc.queryForInt(sql.toString(), st.getArguments());
			} catch ( DataAccessException e ) {
				throw new LogStorageException(e);
			} catch ( MatcherMapperException e ) {
				throw new InvalidCriteriaException(e);
			}
		}
	}

	public void removeEntries(String checksum) throws LogStorageException {
		try {
			jdbc.update("DELETE FROM entry WHERE checksum = ?", checksum);
			jdbc.update("DELETE FROM aggregated_entry WHERE checksum = ?", checksum);
		} catch ( DataAccessException e ) {
			throw new LogStorageException(e);
		}
	}

	/**
	 * Принимает критерии отбора (список обьектов типа {@link org.bazhenov.logging.storage.LogEntryMatcher}),
	 * буффер куда писать WHERE clause и список куда добавлять sql аргументы.
	 *
	 * @param criterias критерии отбора
	 * @return список критериев, которые не могут быть обработаны {@link org.bazhenov.logging.storage.sql.SqlMatcherMapper}'ом
	 */
	private CriteriaStatement fillWhereClause(Collection<LogEntryMatcher> criterias)
		throws MatcherMapperException {

		StringBuilder builder = new StringBuilder();
		List<Object> arguments = new ArrayList<Object>();
		WhereClause where = new WhereClause(builder, arguments);
		Iterator<LogEntryMatcher> iterator = criterias.iterator();
		while ( iterator.hasNext() ) {
			LogEntryMatcher matcher = iterator.next();
			if ( mapper.handle(matcher, where) ) {
				iterator.remove();
			}
		}
		return new CriteriaStatement(builder.toString(), arguments.toArray(), criterias);
	}

	static Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	static java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	private void close(Connection connection) {
		if ( connection != null ) {
			try {
				connection.close();
			} catch ( SQLException e ) {
				log.error("Error occured while closing connection", e);
			}
		}
	}

	private void close(Statement statement) {
		if ( statement != null ) {
			try {
				statement.close();
			} catch ( SQLException e ) {
				log.error("Error occured while closing statement", e);
			}
		}
	}

	private void close(ResultSet resultSet) {
		if ( resultSet != null ) {
			try {
				resultSet.close();
			} catch ( SQLException e ) {
				log.error("Error occured while closing result set", e);
			}
		}
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
}