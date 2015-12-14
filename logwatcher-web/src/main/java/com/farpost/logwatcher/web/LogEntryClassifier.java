package com.farpost.logwatcher.web;

import com.farpost.logwatcher.LogEntry;

public interface LogEntryClassifier {

	String getEntryCssClass(LogEntry entry);
}
