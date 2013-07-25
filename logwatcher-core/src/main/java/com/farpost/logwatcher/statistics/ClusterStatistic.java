package com.farpost.logwatcher.statistics;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Severity;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ClusterStatistic {

	void registerEvent(String applicationId, DateTime date, Checksum checksum, Severity severity);

	Set<String> getActiveApplications();

	ByDayStatistic getByDayStatistic(String applicationId, Checksum checksum);

	Collection<Checksum> getActiveClusterChecksums(String applicationId, LocalDate date);

	MinuteVector getMinuteVector(String applicationId, Checksum checksum);

	Map<Severity, Integer> getSeverityStatistics(String applicationId, LocalDate date);
}
