package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Severity;
import com.google.common.base.Function;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.farpost.logwatcher.SeverityUtils.forName;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;
import static org.joda.time.LocalDate.fromDateFields;

public class SqlClusterStatisticImpl implements ClusterStatistic {

	private final JdbcTemplate template;
	private static final RowMapper<Checksum> createChecksum = new RowMapper<Checksum>() {
		@Override
		public Checksum mapRow(ResultSet rs, int rowNum) throws SQLException {
			return Checksum.fromHexString(rs.getString(1));
		}
	};
	private Function<String, String> toLowerCase = new Function<String, String>() {
		@Override
		public String apply(String input) {
			return input.toLowerCase();
		}
	};

	public SqlClusterStatisticImpl(DataSource ds) {
		template = new JdbcTemplate(checkNotNull(ds));
	}

	@Override
	public synchronized void registerEvent(String applicationId, DateTime date, Checksum checksum, Severity severity) {
		checkNotNull(applicationId);
		checkNotNull(date);
		checkNotNull(checksum);
		checkNotNull(severity);

		updateGeneralStatistics(applicationId, date, checksum);
		updateDayStatistics(applicationId, date, checksum, severity);

		MinuteVector v = getMinuteVector(applicationId, checksum);
		v.increment(date);
		template.update("UPDATE cluster_general_stat SET minute_vector = ? WHERE application = ? AND checksum = ?",
			v.toByteArray(), applicationId, checksum.toString());
	}

	private void updateGeneralStatistics(String applicationId, DateTime date, Checksum checksum) {
		String dt = sqlDateTime(date);
		int aff = template.update(
			"UPDATE cluster_general_stat " +
				"SET last_seen = CASE ? > last_seen WHEN TRUE THEN ? ELSE last_seen END, " +
				"first_seen = CASE ? < first_seen WHEN TRUE THEN ? ELSE first_seen END " +
				"WHERE application = ? AND checksum = ?",
			dt, dt, dt, dt, applicationId, checksum.toString());
		if (aff <= 0) {
			template.update(
				"INSERT INTO cluster_general_stat (first_seen, last_seen, application, checksum) VALUES (?, ?, ?, ?)",
				dt, dt, applicationId, checksum.toString());
		}
	}

	private void updateDayStatistics(String applicationId, DateTime date, Checksum checksum, Severity severity) {
		int affected = template.update(
			"UPDATE cluster_day_stat SET count = count + 1 WHERE application = ? AND date = ? AND checksum = ? ",
			applicationId, sqlDate(date.toLocalDate()), checksum.toString());
		if (affected <= 0) {
			template.update("INSERT INTO cluster_day_stat (application, date, checksum, severity, count) VALUES " +
				"(?, ?, ?, ?, 1)", applicationId, sqlDate(date.toLocalDate()), checksum.toString(), severity.toString());
		}
	}

	@Override
	public Map<Severity, Integer> getSeverityStatistics(String applicationId, LocalDate date) {
		ResultSetExtractor<Map<Severity, Integer>> rse = new ResultSetExtractor<Map<Severity, Integer>>() {
			@Override
			public Map<Severity, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<Severity, Integer> result = newHashMap();
				while (rs.next()) {
					Severity s = forName(rs.getString("severity")).get();
					int i = rs.getInt("count");
					result.put(s, i);
				}
				return result;
			}
		};
		return template.query("SELECT severity, SUM(count) as count FROM cluster_day_stat WHERE " +
			"application = ? AND date = ? GROUP BY severity", rse, applicationId, sqlDate(date));
	}

	@Override
	public Set<String> getActiveApplications() {
		Iterable<String> applications = template.queryForList("SELECT application FROM cluster_day_stat WHERE date > ?",
			String.class, sqlDate(LocalDate.now().minusDays(7)));
		return newTreeSet(from(applications).transform(toLowerCase));
	}

	@Override
	public MinuteVector getMinuteVector(String applicationId, Checksum checksum) {
		byte[] vector = template.queryForObject(
			"SELECT minute_vector FROM cluster_general_stat WHERE application = ? AND checksum = ?",
			byte[].class, applicationId, checksum.toString());
		return vector == null
			? new MinuteVector()
			: new MinuteVector(vector);
	}

	@Override
	public ByDayStatistic getByDayStatistic(String applicationId, Checksum checksum) {
		SqlRowSet rowSet = template.queryForRowSet(
			"SELECT date, count FROM cluster_day_stat WHERE application = ? AND checksum = ?",
			applicationId, checksum.toString());
		Map<LocalDate, Integer> counts = newHashMap();
		while (rowSet.next()) {
			counts.put(fromDateFields(rowSet.getDate("date")), rowSet.getInt("count"));
		}

		// TODO можно выполнить за один запрос
		Date lastSeen = template.queryForObject(
			"SELECT last_seen FROM cluster_general_stat WHERE application = ? AND checksum = ?", Date.class,
			applicationId, checksum.toString());
		Date firstSeen = template.queryForObject(
			"SELECT first_seen FROM cluster_general_stat WHERE application = ? AND checksum = ?", Date.class,
			applicationId, checksum.toString());

		return new ByDayStatistic(applicationId, checksum, new DateTime(firstSeen), new DateTime(lastSeen), counts);
	}

	@Override
	public Collection<Checksum> getActiveClusterChecksums(String applicationId, LocalDate date) {
		return template.query("SELECT checksum FROM cluster_day_stat WHERE date = ? AND application = ?", createChecksum,
			sqlDate(date), applicationId);
	}

	private static String sqlDate(LocalDate date) {
		return date.toString("yyyy-MM-dd");
	}

	private static String sqlDateTime(DateTime date) {
		return date.toString("yyyy-MM-dd HH:mm:ss");
	}
}
