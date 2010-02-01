package org.bazhenov.logging;

import org.bazhenov.logging.storage.LogEntryBuilder;

public class TestSupport {

	public static LogEntryBuilder entry() {
		return new LogEntryBuilder();
	}
}
