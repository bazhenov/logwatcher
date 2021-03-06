package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.Severity.error;
import static com.farpost.logwatcher.Severity.info;
import static com.farpost.logwatcher.TestUtils.checksum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ClusterDaoTest {

	ClusterDao dao;

	@BeforeMethod
	public void setUp() {
		dao = createDao();
	}

	protected abstract ClusterDao createDao();

	@Test
	public void shouldBeAbleToRegisterNewCluster() {
		Checksum checksum = checksum(1, 2, 3);
		String applicationId = "foo";
		assertThat(dao.findCluster(applicationId, checksum), nullValue());
		Cluster cluster = new Cluster(applicationId, error, "Message title", checksum);
		dao.registerCluster(cluster);
		// Intentionally use another instance of checksum to check non-reference equality
		assertThat(dao.findCluster(applicationId, checksum(1, 2, 3)), equalTo(cluster));
	}

	@Test
	public void shouldBeAbleToFindClusterInfo() {
		String applicationId = "foo";
		Checksum checksum = checksum(1, 2, 3);
		Cluster cluster = new Cluster(applicationId, info, "title", checksum);
		cluster.setGroup("gr1");
		cluster.setCauseType("Exception");
		dao.registerCluster(cluster);

		Cluster clusterCopy = dao.findCluster(applicationId, checksum);
		assertThat(clusterCopy, equalTo(cluster));
	}

	@Test
	public void shouldBeAbleToUpdateClusterInfo() {
		String applicationId = "foo";
		Checksum checksum = checksum(1, 2, 3);
		Cluster cluster = new Cluster(applicationId, info, "title", checksum);
		cluster.setGroup("gr1");
		cluster.setCauseType("Exception");
		dao.registerCluster(cluster);

		Cluster clusterCopy = dao.findCluster(applicationId, checksum);
		assertThat(clusterCopy, equalTo(cluster));

		cluster.setCauseType(null);
		dao.registerCluster(cluster);

		clusterCopy = dao.findCluster(applicationId, checksum);
		assertThat(clusterCopy, equalTo(cluster));
	}

	@Test
	public void shouldBeAbleToEditCluster() {
		String applicationId = "foo";
		Checksum checksum = checksum(1, 2, 3);
		Cluster cluster = new Cluster(applicationId, info, "title", checksum);
		dao.registerCluster(cluster);

		dao.changeCluster(applicationId, checksum, c -> {
			c.setTitle("New title");
			c.setIssueKey("PRJ-12");
			c.setDescription("Some meaningful text");
		});

		Cluster clusterCopy = dao.findCluster(applicationId, checksum);
		assertThat(clusterCopy.getTitle(), is("New title"));
		assertThat(clusterCopy.getIssueKey(), is("PRJ-12"));
		assertThat(clusterCopy.getDescription(), is("Some meaningful text"));
	}
}
