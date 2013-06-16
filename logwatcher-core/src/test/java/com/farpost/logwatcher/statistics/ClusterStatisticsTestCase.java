package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.TestUtils.checksum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public abstract class ClusterStatisticsTestCase {

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
		stat.registerEvent("application", dateTime, checksum);
		DayStatistic dayStat = stat.getDayStatistic("application", checksum, new LocalDate(dateTime));

		assertThat(dayStat, equalTo(new DayStatistic("application", checksum, new LocalDate(dateTime), dateTime, 1)));
	}
}
