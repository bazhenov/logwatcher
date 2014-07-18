package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.google.common.base.Function;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.Severity.error;
import static com.farpost.logwatcher.Severity.info;
import static com.farpost.logwatcher.TestUtils.checksum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
		assertThat(dao.isClusterRegistered(applicationId, checksum), is(false));
		Cluster cluster = new Cluster(applicationId, error, "Message title", checksum);
		dao.registerCluster(cluster);
		// Intentionally use another instance of checksum to check non-reference equality
		assertThat(dao.isClusterRegistered(applicationId, checksum(1, 2, 3)), is(true));
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

		dao.changeCluster(applicationId, checksum, new Function<Cluster, Void>() {
			@Override
			public Void apply(Cluster c) {
				c.setTitle("New title");
				c.setIssueKey("PRJ-12");
				c.setDescription("Some meaningful text");
				return null;
			}
		});

		Cluster clusterCopy = dao.findCluster(applicationId, checksum);
		assertThat(clusterCopy.getTitle(), is("New title"));
		assertThat(clusterCopy.getIssueKey(), is("PRJ-12"));
		assertThat(clusterCopy.getDescription(), is("Some meaningful text"));
	}
}
