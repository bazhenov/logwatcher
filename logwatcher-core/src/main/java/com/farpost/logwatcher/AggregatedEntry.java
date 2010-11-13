package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.Severity;

/**
 * Представляет собой результат аггрегирования нескольких записей {@link org.bazhenov.logging.LogEntry}
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