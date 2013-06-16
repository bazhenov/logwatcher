package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Set;

public interface ClusterStatistic {

	void registerEvent(String applicationId, DateTime date, Checksum checksum);

	Set<String> getActiveApplications();

	DayStatistic getDayStatistic(String applicationId, Checksum checksum, LocalDate date);
}
