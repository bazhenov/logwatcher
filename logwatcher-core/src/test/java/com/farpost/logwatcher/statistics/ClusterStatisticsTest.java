package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Severity;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.farpost.logwatcher.Severity.error;
import static com.farpost.logwatcher.Severity.warning;
import static com.farpost.logwatcher.TestUtils.checksum;
import static com.google.common.collect.Sets.newTreeSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ClusterStatisticsTest {

	private ClusterStatistic stat;

	protected abstract ClusterStatistic createClusterStatistic();

	@BeforeMethod
	public void setUp() {
		stat = createClusterStatistic();
	}

	@Test
	public void shouldBeAbleToTrackActiveApplications() {
		stat.registerEvent("Bapplication", new DateTime(), checksum(1, 3, 4), error);
		stat.registerEvent("application", new DateTime(), checksum(1, 3, 4), error);
		stat.registerEvent("Application", new DateTime(), checksum(1, 3, 4), error);

		Set<String> expectedOutput = newTreeSet();
		expectedOutput.add("application");
		expectedOutput.add("bapplication");
		assertThat(stat.getActiveApplications(), equalTo(expectedOutput));
	}

	@Test
	public void shouldBeAbleToTrackDayBasedStatistic() {
		Checksum checksum = checksum(1, 3, 4);
		DateTime dateTime = new DateTime();
		String applicationId = "application";
		stat.registerEvent(applicationId, dateTime, checksum, error);
		stat.registerEvent(applicationId, dateTime, checksum, error);
		stat.registerEvent(applicationId, dateTime.minusDays(1), checksum, error);
		ByDayStatistic dayStat = stat.getByDayStatistic(applicationId, checksum);

		assertThat(dayStat, notNullValue());
		assertThat(dayStat.getApplicationId(), is(applicationId));
		assertThat(dayStat.getChecksum(), is(checksum));
		assertThat(dayStat.getCount(LocalDate.now()), is(2));
		assertThat(dayStat.getCount(LocalDate.now().minusDays(1)), is(1));
		assertThat((double) dayStat.getLastSeenAt().getMillis(), closeTo(dateTime.getMillis(), 1000d));
	}

	@Test
	public void shouldBeAbleToTrackMinuteStatistics() {
		stat.registerEvent("foo", new DateTime(), checksum(1, 3, 5), error);
		MinuteVector v = stat.getMinuteVector("foo", checksum(1, 3, 5));
		assertThat(v.get(0), is(1L));
		assertThat(v.get(-1), is(0L));
	}

	@Test
	public void shouldBeAbleToTrackSeverityStatistics() {
		stat.registerEvent("foo", new DateTime(), checksum(1, 2, 3), error);
		stat.registerEvent("foo", new DateTime(), checksum(1, 3, 5), warning);
		stat.registerEvent("foo", new DateTime(), checksum(1, 3, 5), warning);

		Map<Severity, Integer> statistics = stat.getSeverityStatistics("foo", new LocalDate());
		assertThat(statistics, hasEntry(error, 1));
		assertThat(statistics, hasEntry(warning, 2));
		assertThat(statistics.size(), is(2));
	}

	@Test
	public void getActiveChecksumsForADate() {
		Checksum checksum = checksum(1, 3, 4);
		DateTime dateTime = new DateTime();
		stat.registerEvent("application", dateTime, checksum, error);
		Collection<Checksum> checksums = stat.getActiveClusterChecksums("application", new LocalDate(dateTime));
		assertThat(checksums, hasItems(checksum));
	}
}
