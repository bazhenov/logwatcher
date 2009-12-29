package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

import java.util.Map;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link org.bazhenov.logging.LogEntry}
 */
public interface AggregatedEntry {

	DateTime getLastTime();

	int getCount();

	String getMessage();

	Cause getSampleCause();

	String getChecksum();
}