package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;

public interface LogEntryListener {

	void onEntry(LogEntry entry);
}
