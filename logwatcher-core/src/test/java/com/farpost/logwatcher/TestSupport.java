package com.farpost.logwatcher;

import com.farpost.logwatcher.storage.LogEntryBuilder;

public class TestSupport {

	public static LogEntryBuilder entry() {
		return new LogEntryBuilder();
	}
}
