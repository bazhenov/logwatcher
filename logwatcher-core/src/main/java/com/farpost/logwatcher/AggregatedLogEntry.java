package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;
import com.farpost.logwatcher.LogEntry;

import java.util.Map;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link com.farpost.logwatcher.LogEntry}
 */
public interface AggregatedLogEntry {

	DateTime getLastTime();

	int getCount();

	String getGroup();

	LogEntry getSampleEntry();

	Map<String, AggregatedAttribute> getAttributes();
}
