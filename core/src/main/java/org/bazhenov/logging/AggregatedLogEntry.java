package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link LogEntry}
 */
public interface AggregatedLogEntry {

	DateTime getLastTime();

	int getCount();

	LogEntry getSampleEntry();
}
