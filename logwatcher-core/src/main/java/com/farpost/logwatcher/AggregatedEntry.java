package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link com.farpost.logwatcher.LogEntry}
 */
public interface AggregatedEntry {

	DateTime getLastTime();

	Severity getSeverity();

	int getCount();

	String getMessage();

	String getApplicationId();

	Cause getSampleCause();

	String getChecksum();
}