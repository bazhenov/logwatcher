package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link com.farpost.logwatcher.LogEntry}
 */
public interface AggregatedLogEntry {

	DateTime getLastTime();

	int getCount();

	String getGroup();

	LogEntry getSampleEntry();
}
