package com.farpost.logwatcher.cluster;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.farpost.logwatcher.Severity;
import com.google.common.base.Strings;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlClusterDaoTest extends ClusterDaoTest {

	private EmbeddedDatabase db;

	@AfterMethod
	public void tearDown() {
		db.shutdown();
	}

	@Override
	protected ClusterDao createDao() {
		db = new EmbeddedDatabaseBuilder().
			setType(EmbeddedDatabaseType.H2).
			addScript("classpath:com/farpost/logwatcher/storage/schema.sql").
			build();
		return new SqlClusterDao(db);
	}

	@Test
	public void shouldTrimLongEntries() {
		String message = Strings.repeat("message", 40);
		Checksum checksum = fromHexString("02fe");
		String applicationId = "foo";
		dao.registerCluster(new Cluster(applicationId, Severity.debug, message, checksum));
		Cluster cluster = dao.findCluster(applicationId, checksum);
		assertThat(cluster.getTitle().length(), is(255));
	}
}
