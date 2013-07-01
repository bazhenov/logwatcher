package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;

import static com.farpost.logwatcher.TestUtils.checksum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ClusterStatisticsTest {

	ClusterStatistic stat;

	@BeforeMethod
	public void setUp() {
		stat = createClusterStatistic();
	}

	protected abstract ClusterStatistic createClusterStatistic();

	@Test
	public void shouldBeAbleToTrackActiveApplications() {
		stat.registerEvent("application", new DateTime(), checksum(1, 3, 4));
		stat.registerEvent("Application", new DateTime(), checksum(1, 3, 4));

		assertThat(stat.getActiveApplications(), contains("application"));
	}

	@Test
	public void shouldBeAbleToTrackDayBasedStatistic() {
		Checksum checksum = checksum(1, 3, 4);
		DateTime dateTime = new DateTime();
		String applicationId = "application";
		stat.registerEvent(applicationId, dateTime, checksum);
		stat.registerEvent(applicationId, dateTime, checksum);
		stat.registerEvent(applicationId, dateTime.minusDays(1), checksum);
		ByDayStatistic dayStat = stat.getByDayStatistic(applicationId, checksum);

		assertThat(dayStat, notNullValue());
		assertThat(dayStat.getApplicationId(), is(applicationId));
		assertThat(dayStat.getChecksum(), is(checksum));
		assertThat(dayStat.getCount(LocalDate.now()), is(2));
		assertThat(dayStat.getCount(LocalDate.now().minusDays(1)), is(1));
	}

	@Test
	public void getActiveChecksumsForADate() {
		Checksum checksum = checksum(1, 3, 4);
		DateTime dateTime = new DateTime();
		stat.registerEvent("application", dateTime, checksum);
		Collection<Checksum> checksums = stat.getActiveClusterChecksums("application", new LocalDate(dateTime));
		assertThat(checksums, hasItems(checksum));
	}
}
