package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.google.common.base.Function;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
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
	public synchronized void registerEvent(String applicationId, DateTime date, Checksum checksum) {
		updateGeneralStatistics(applicationId, date, checksum);
		updateDayStatistics(applicationId, date, checksum);

		MinuteVector v = getMinuteVector(applicationId, checksum);
		v.increment(date);
		template.update("UPDATE cluster_general_stat SET minute_vector = ? WHERE application = ? AND checksum = ?",
			v.toByteArray(), applicationId, checksum.toString());
	}

	private void updateGeneralStatistics(String applicationId, DateTime date, Checksum checksum) {
		String dt = date.toString("yyyy-MM-dd HH:mm:ss");
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

	private void updateDayStatistics(String applicationId, DateTime date, Checksum checksum) {
		int affected = template.update(
			"UPDATE cluster_day_stat SET count = count + 1 WHERE application = ? AND date = ? AND checksum = ? ",
			applicationId, date.toString("yyyy-MM-dd"), checksum.toString());
		if (affected <= 0) {
			template.update("INSERT INTO cluster_day_stat (application, date, checksum, count) VALUES (?, ?, ?, 1)",
				applicationId, date.toString("yyyy-MM-dd"), checksum.toString());
		}
	}

	@Override
	public Set<String> getActiveApplications() {
		Iterable<String> applications = template.queryForList("SELECT application FROM cluster_day_stat WHERE date > ?",
			String.class, LocalDate.now().minusDays(7).toString("yyyy-MM-dd"));
		return from(applications).transform(toLowerCase).toSet();
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

		Date lastSeen = template.queryForObject(
			"SELECT last_seen FROM cluster_general_stat WHERE application = ? AND checksum = ?", Date.class,
			applicationId, checksum.toString());

		return new ByDayStatistic(applicationId, checksum, new DateTime(lastSeen), counts);
	}

	@Override
	public Collection<Checksum> getActiveClusterChecksums(String applicationId, LocalDate date) {
		return template.query("SELECT checksum FROM cluster_day_stat WHERE date = ? AND application = ?", createChecksum,
			date.toString("yyyy-MM-dd"), applicationId);
	}
}
