package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.LogEntry;

import java.util.Map;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link org.bazhenov.logging.LogEntry}
 */
public interface AggregatedLogEntry {

	DateTime getLastTime();

	int getCount();

	String getGroup();

	LogEntry getSampleEntry();

	Map<String, AggregatedAttribute> getAttributes();
}
