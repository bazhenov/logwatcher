package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.aggregator.Aggregator;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.storage.spi.AnnotationDrivenMatcherMapperImpl;
import com.farpost.logwatcher.storage.spi.MatcherMapper;
import com.farpost.logwatcher.storage.spi.MatcherMapperException;
import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static com.farpost.logwatcher.storage.MatcherUtils.isMatching;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

import com.farpost.timepoint.Date;

public class SqlLogStorage implements LogStorage {

	private final Aggregator aggregator;
	private final DataSource datasource;
	private final ChecksumCalculator checksumCalculator;
	private final Marshaller marshaller;
	private final MatcherMapper<SqlWhereStatement> mapper;
	private final SimpleJdbcTemplate jdbc;
	private final Logger log = LoggerFactory.getLogger(SqlLogStorage.class);
	private final ParameterizedRowMapper<AggregatedEntry> aggregateEntryMapper;
	private final ParameterizedRowMapper<LogEntry> entryMapper;

	public SqlLogStorage(Aggregator aggregator, DataSource datasource, Marshaller marshaller,
											 ChecksumCalculator checksumCalculator)
		throws IOException, SQLException {
		this.aggregator = aggregator;
		this.marshaller = marshaller;
		this.mapper = new AnnotationDrivenMatcherMapperImpl<SqlWhereStatement>(new SqlMatcherMapperRules());
		this.datasource = datasource;
		this.checksumCalculator = checksumCalculator;
		this.jdbc = new SimpleJdbcTemplate(datasource);
		this.aggregateEntryMapper = new CreateAggregatedEntryRowMapper(marshaller);
		this.entryMapper = new CreateEntryRowMapper(marshaller);
	}

	public synchronized void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			LogEntryImpl impl = (LogEntryImpl) entry;
			Timestamp entryTimestamp = timestamp(impl.getDate());
			java.sql.Date entryDate = date(impl.getDate());
			String checksum = checksumCalculator.calculateChecksum(impl);
			impl.setChecksum(checksum);
			byte[] marshalledEntry = marshaller.marshall(impl);
			jdbc.update(
				"INSERT INTO entry (time, date, checksum, category, severity, application_id, content) VALUES (?, ?, ?, ?, ?, ?, ?)",
				entryTimestamp, entryDate, checksum, impl.getCategory(),
				impl.getSeverity().getCode(), impl.getApplicationId(), marshalledEntry);

			int affectedRows = jdbc.update(
				"UPDATE aggregated_entry SET count = count + 1, last_time = ? WHERE date = ? AND checksum = ?",
				entryTimestamp, entryDate, checksum);
			if (affectedRows == 0) {
				jdbc.update(
					"INSERT INTO aggregated_entry (date, checksum, last_time, category, severity, application_id, count, content) VALUES (?, ?, ?, ?, ?, ?, 1, ?)",
					entryDate, checksum, entryTimestamp, impl.getCategory(),
					impl.getSeverity().getCode(), impl.getApplicationId(), marshalledEntry);
			}

			if (log.isDebugEnabled()) {
				log.debug("Entry with checksum: " + checksum + " wrote to database");
			}

		} catch (DataAccessException e) {
			throw new LogStorageException(e);
		}
	}

	@Override
	public int removeOldEntries(Date date) throws LogStorageException {
		try {
			return jdbc.update("DELETE FROM entry WHERE date < ?", date(date));
		} catch (DataAccessException e) {
			throw new LogStorageException(e);
		}
	}

	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {
		try {
			CriteriaStatement st = fillWhereClause(criterias);
			if (st.haveLateBoundMatchers()) {
				throw new InvalidCriteriaException(st.getLateBoundMatchers());
			}
			String sql = "SELECT content FROM entry l WHERE " + st.getWhereClause() + " ORDER BY l.time DESC LIMIT 100";
			return jdbc.query(sql, entryMapper, st.getArguments());
		} catch (MatcherMapperException e) {
			throw new LogStorageException(e);
		}
	}

	private boolean enableServerSideCursor(PreparedStatement statement) {
		try {
			statement.setFetchSize(Integer.MIN_VALUE);
			return true;
		} catch (SQLException e) {
			log.warn("Server side cursors are not supported. Disabling cursor");
			return false;
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
			enableServerSideCursor(statement);
			fill(statement, st.getArguments());

			result = statement.executeQuery();
			Collection<LogEntryMatcher> lateBoundMatchers = st.getLateBoundMatchers();
			while (result.next()) {
				LogEntry entry = marshaller.unmarshall(result.getBytes("content"));
				if (isMatching(entry, lateBoundMatchers)) {
					visitor.visit(entry);
				}
			}
		} catch (SQLException e) {
			throw new LogStorageException(e);
		} catch (MatcherMapperException e) {
			throw new LogStorageException(e);
		} finally {
			close(result);
			close(statement);
			close(connection);
		}
	}

	@Override
	public Set<String> getUniquieApplicationIds(Date date) {
		if (date == null) {
			throw new NullPointerException("Date should not be null");
		}
		List<String> ids = jdbc.query("SELECT application_id FROM aggregated_entry WHERE date = ?", takeFirst(), date(date));
		Set<String> applicationIds = new HashSet<String>();
		for (String id : ids) {
			applicationIds.add(id);
		}
		return applicationIds;
	}

	private RowMapper<String> takeFirst() {
		return new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		};
	}

	public List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date, Severity severity) {
		return jdbc.query(
			"SELECT checksum, application_id, last_time, count, severity, content FROM aggregated_entry WHERE application_id = ? AND date = ? AND severity >= ?",
			aggregateEntryMapper, applicationId, date(date), severity.getCode());
	}

	private void fill(PreparedStatement statement, Object[] arguments) throws SQLException {
		int i = 1;
		for (Object obj : arguments) {
			if (obj instanceof String) {
				statement.setString(i++, (String) obj);
			} else if (obj instanceof java.sql.Date) {
				statement.setDate(i++, (java.sql.Date) obj);
			} else if (obj instanceof Timestamp) {
				statement.setTimestamp(i++, (Timestamp) obj);
			} else if (obj instanceof Integer) {
				statement.setInt(i++, (Integer) obj);
			} else {
				throw new RuntimeException("Unknown argument type: " + obj.getClass().getName());
			}
		}
	}

	public int countEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `entry` l");
		if (criterias == null || criterias.size() <= 0) {
			return jdbc.queryForInt(sql.toString());
		} else {
			try {
				CriteriaStatement st = fillWhereClause(criterias);
				if (st.haveLateBoundMatchers()) {
					throw new InvalidCriteriaException(st.getLateBoundMatchers());
				}
				sql.append(" WHERE ").append(st.getWhereClause());
				return jdbc.queryForInt(sql.toString(), st.getArguments());
			} catch (DataAccessException e) {
				throw new LogStorageException(e);
			} catch (MatcherMapperException e) {
				throw new InvalidCriteriaException(e);
			}
		}
	}

	public void removeEntriesWithChecksum(String checksum) throws LogStorageException {
		try {
			jdbc.update("DELETE FROM entry WHERE checksum = ?", checksum);
			jdbc.update("DELETE FROM aggregated_entry WHERE checksum = ?", checksum);
		} catch (DataAccessException e) {
			throw new LogStorageException(e);
		}
	}

	/**
	 * Принимает критерии отбора (список обьектов типа {@link com.farpost.logwatcher.storage.LogEntryMatcher}),
	 * буффер куда писать WHERE clause и список куда добавлять sql аргументы.
	 *
	 * @param criterias критерии отбора
	 * @return список критериев, которые не могут быть обработаны {@link MatcherMapper}'ом
	 * @throws MatcherMapperException если не все критерии могут быть обработаны хранилищем
	 */
	private CriteriaStatement fillWhereClause(Collection<LogEntryMatcher> criterias)
		throws MatcherMapperException {

		StringBuilder builder = new StringBuilder();
		List<Object> arguments = new ArrayList<Object>();
		Iterator<LogEntryMatcher> iterator = criterias.iterator();
		while (iterator.hasNext()) {
			LogEntryMatcher matcher = iterator.next();
			SqlWhereStatement statement = mapper.handle(matcher);
			if (statement != null) {
				iterator.remove();
				if (builder.length() > 0) {
					builder.append(" AND ");
				}
				builder.append(statement.getStatement());
				arguments.addAll(statement.getArguments());
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
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing connection", e);
			}
		}
	}

	private void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing statement", e);
			}
		}
	}

	private void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing result set", e);
			}
		}
	}
}