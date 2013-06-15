package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Cluster;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ClusterDaoTestCase {

	ClusterDao dao;

	@BeforeMethod
	public void setUp() {
		dao = createDao();
	}

	protected abstract ClusterDao createDao();

	@Test
	public void shouldBeAbleToRegisterNewCluster() {
		byte[] checksum = checksum(1, 2, 3);
		assertThat(dao.isClusterRegistered(checksum), is(false));
		Cluster cluster = new Cluster("Message title", checksum);
		dao.registerCluster(cluster);
		// Intentionally use another instance of checksum to check non-reference equality
		assertThat(dao.isClusterRegistered(checksum(1, 2, 3)), is(true));
	}

	private static byte[] checksum(int... input) {
		byte[] bytes = new byte[input.length];
		for (int i = 0; i < input.length; i++) {
			checkArgument(input[i] < 256);
			bytes[i] = (byte) input[i];
		}
		return bytes;
	}
}
