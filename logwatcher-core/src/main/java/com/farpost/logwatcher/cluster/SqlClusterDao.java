package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.function.Consumer;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.SeverityUtils.forName;
import static com.google.common.base.Preconditions.checkArgument;

public class SqlClusterDao implements ClusterDao {

	private final JdbcTemplate template;
	private RowMapper<Cluster> createCluster = (rs, rowNum) -> {
		Cluster cluster = new Cluster(rs.getString("application"), rs.getString("title"),
			fromHexString(rs.getString("checksum")), rs.getString("description"), rs.getString("issue_key"),
			forName(rs.getString("severity")).get());
		cluster.setCauseType(rs.getString("cause_type"));
		cluster.setGroup(rs.getString("group"));
		return cluster;
	};

	public SqlClusterDao(DataSource dataSource) {
		template = new JdbcTemplate(dataSource);
	}

	private boolean isClusterRegistered(String applicationId, Checksum checksum) {
		return template.queryForObject("SELECT COUNT(*) FROM cluster WHERE application = ? AND checksum = ?", Integer.class,
			applicationId, checksum.toString()) > 0;
	}

	@Override
	public void registerCluster(final Cluster cluster) {
		String title = cluster.getTitle().length() > 255
			? cluster.getTitle().substring(0, 255)
			: cluster.getTitle();
		if (isClusterRegistered(cluster.getApplicationId(), cluster.getChecksum())) {
			changeCluster(cluster.getApplicationId(), cluster.getChecksum(), input -> {
				input.setTitle(cluster.getTitle());
				input.setCauseType(cluster.getCauseType());
				input.setGroup(cluster.getGroup());
			});
		} else {
			template.update("INSERT INTO cluster (application, checksum, description, severity, title, issue_key, " +
					"`group`, cause_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				cluster.getApplicationId(), cluster.getChecksum().toString(), cluster.getDescription(),
				cluster.getSeverity().toString(), title, cluster.getIssueKey(), cluster.getGroup(), cluster.getCauseType());
		}
	}

	@Override
	public Cluster findCluster(String applicationId, Checksum checksum) {
		try {
			return template.queryForObject("SELECT * FROM cluster WHERE application = ? AND checksum = ?", createCluster,
				applicationId, checksum.toString());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public void changeCluster(String applicationId, Checksum checksum, Consumer<Cluster> f) {
		Cluster cluster = findCluster(applicationId, checksum);
		checkArgument(cluster != null, "Cluster not found");
		f.accept(cluster);
		template.update("UPDATE cluster SET title = ?, issue_key = ?, description = ?, `group` = ?, cause_type = ? " +
				"WHERE application = ? AND checksum = ?",
			cluster.getTitle(), cluster.getIssueKey(), cluster.getDescription(), cluster.getGroup(), cluster.getCauseType(),
			applicationId, checksum.toString());
	}
}
