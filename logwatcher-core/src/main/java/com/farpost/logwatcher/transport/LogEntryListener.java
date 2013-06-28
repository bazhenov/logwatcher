package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;

public interface LogEntryListener {

	public void onEntry(LogEntry entry);
}
