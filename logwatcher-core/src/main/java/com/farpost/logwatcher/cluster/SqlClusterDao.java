package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.Severity.forName;

public class SqlClusterDao implements ClusterDao {

	private final JdbcTemplate template;
	private RowMapper<Cluster> createCluster = new RowMapper<Cluster>() {
		@Override
		public Cluster mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Cluster(rs.getString("application"), rs.getString("title"), fromHexString(rs.getString("checksum")),
				rs.getString("description"), rs.getString("issue_key"), forName(rs.getString("severity")));
		}
	};

	public SqlClusterDao(DataSource dataSource) {
		template = new JdbcTemplate(dataSource);
	}

	@Override
	public boolean isClusterRegistered(String applicationId, Checksum checksum) {
		return template.queryForInt("SELECT COUNT(*) FROM cluster WHERE application = ? AND checksum = ?",
			applicationId, checksum.toString()) > 0;
	}

	@Override
	public void registerCluster(Cluster cluster) {
		template.update("INSERT INTO cluster (application, checksum, description, severity, title, issue_key) " +
			"VALUES (?, ?, ?, ?, ?, ?)",
			cluster.getApplicationId(), cluster.getChecksum().toString(), cluster.getDescription(),
			cluster.getSeverity().toString(), cluster.getTitle(), cluster.getIssueKey());
	}

	@Override
	public Cluster findCluster(String applicationId, Checksum checksum) {
		return template.queryForObject("SELECT * FROM cluster WHERE application = ? AND checksum = ?", createCluster,
			applicationId, checksum.toString());
	}
}
