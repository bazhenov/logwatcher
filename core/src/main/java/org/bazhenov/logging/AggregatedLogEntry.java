package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

public interface AggregatedLogEntry {

	DateTime getLastTime();

	int getCount();

	LogEntry getSampleEntry();
}
